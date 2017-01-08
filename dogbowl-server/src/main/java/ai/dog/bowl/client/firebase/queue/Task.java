/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.firebase.queue;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.ServerValue;
import com.firebase.client.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.validation.constraints.NotNull;

public class Task {
  public static final String STATE_KEY = "_state";
  public static final String STATE_CHANGED_KEY = "_state_changed";
  public static final String OWNER_KEY = "_owner";
  public static final String ERROR_DETAILS_KEY = "_error_details";
  public static final String ERROR_DETAILS_ATTEMPTS_KEY = "attempts";
  public static final String ERROR_DETAILS_PREVIOUS_STATE_KEY = "previous_state";
  public static final String ERROR_KEY = "error";
  public static final String ERROR_STACK_KEY = "error_stack";
  public static final String ORIGINAL_TASK_KEY = "original_task";
  private static final Logger logger = LoggerFactory.getLogger(Task.class);
  private static final String ACTION_RESOLVED = "resolve";
  private static final String ACTION_REJECTED = "reject";
  private static final String ACTION_ABORTED = "abort";
  private final Firebase taskRef;
  private final String ownerId;
  private final Map<String, Object> data;
  private final TaskSpec taskSpec;
  private final TaskReset taskReset;
  private final ValidityChecker validityChecker;
  private final boolean suppressStack;
  private final Object actionLock = new Object();
  private boolean processing;
  private CountDownLatch completionLatch;
  private WeakReference<Thread> processingThreadRef;
  private volatile boolean interrupted;
  private volatile boolean aborted;
  private volatile boolean completed;
  // this is true if we no longer own the task, or can't take any other action on this task
  private volatile boolean cancelled;

  public Task(@NotNull Firebase taskRef, @NotNull String ownerId, @NotNull Map<String, Object> data, @NotNull TaskSpec taskSpec, @NotNull TaskReset taskReset, @NotNull ValidityChecker validityChecker, boolean suppressStack) {
    this.taskRef = taskRef;
    this.ownerId = ownerId;
    this.data = data;
    this.taskSpec = taskSpec;
    this.taskReset = taskReset;
    this.validityChecker = validityChecker;
    this.suppressStack = suppressStack;
  }

  private static String getStackTraceAsString(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    try {
      t.printStackTrace(pw);
      return sw.toString();
    } finally {
      pw.close();
    }
  }

  /*package*/ void process(TaskProcessor taskProcessor) {
    if(processing) {
      throw new IllegalStateException("Cannot process a task more than once");
    }

    processing = true;

    if(Thread.currentThread().isInterrupted()) {
      interrupted = true;
      return;
    }

    processingThreadRef = new WeakReference<Thread>(Thread.currentThread());

    completionLatch = new CountDownLatch(1);

    try {
      taskProcessor.process(this);

      if(Thread.currentThread().isInterrupted()) {
        synchronized (actionLock) {
          if(canTakeAction()) {
            interrupted = true;

            completionLatch.countDown();

            return;
          }
        }
      }

      completionLatch.await();
    }
    catch (InterruptedException e) {
      synchronized (actionLock) {
        if(canTakeAction()) {
          interrupted = true;

          completionLatch.countDown();
        }
      }
    }

    //if(interrupted) {
    //  taskReset.reset(taskRef, taskSpec.getInProgressState());
    //}
  }

  public boolean isFinished() {
    return !canTakeAction() && cancelled;
  }

  private boolean canTakeAction() {
    return !interrupted && !aborted && !completed;
  }

  private String getActionError() {
    return (aborted ? "aborted" : interrupted ? "cancelled" : "completed");
  }

  public Map<String, Object> getData() {
    return data;
  }

  public TaskSpec getTaskSpec() {
    return taskSpec;
  }

  protected void abort(String errorMessage, boolean reset) {
    abort(errorMessage, reset, null);
  }

  protected void abort(String errorMessage, boolean reset, final Listener listener) {
    logger.debug("Attempting to abort task " + taskRef.getKey() + " on " + ownerId);

    Thread processingThread = processingThreadRef.get();
    if (processingThread == null || !validityChecker.isValid(processingThread, ownerId)) {
      if (listener != null) {
        listener.onFailure("Couldn't abort this task because it is owned by another worker", false);
      }

      logger.debug("Couldn't abort task " + taskRef.getKey() + " on " + ownerId + " because we no longer own it");

      return;
    }

    synchronized (actionLock) {
      if(canTakeAction()) {
        aborted = true;

        processingThread.interrupt();

        if (reset) {
          taskReset.reset(taskRef, ownerId, taskSpec.getInProgressState(), new TaskReset.Listener() {
            @Override
            public void onReset() {
              completionLatch.countDown();

              logger.debug("Successful abort of task " + taskRef.getKey() + " on " + ownerId);

              if (listener != null) listener.onSuccess();
            }

            @Override
            public void onResetFailed(String error, boolean canRetry) {
              if (!canRetry) {
                cancel();
              }

              if (listener != null) listener.onFailure(error, canRetry);
            }
          });
        } else {
          taskRef.runTransaction(new Transaction.Handler() {
            long retries = 0;

            @Override
            public Transaction.Result doTransaction(MutableData task) {
              @SuppressWarnings("unchecked") Map<String, Object> value = task.getValue(Map.class);
              String ourInProgressState = taskSpec.getInProgressState();
              Object taskState = value.get(STATE_KEY);
              Object taskOwner = value.get(OWNER_KEY);
              boolean ownersMatch = ownerId.equals(taskOwner);
              if ((ourInProgressState == taskState || (ourInProgressState != null && ourInProgressState.equals(taskState))) && ownersMatch) {
                @SuppressWarnings("unchecked") Map<String, Object> errorDetails = (Map<String, Object>) value.get(ERROR_DETAILS_KEY);
                if (errorDetails == null) {
                  errorDetails = new HashMap<String, Object>();
                }

                int attempts = 0;
                Integer currentAttempts = (Integer) errorDetails.get(ERROR_DETAILS_ATTEMPTS_KEY);
                if (currentAttempts == null) {
                  currentAttempts = 0;
                }
                String currentPreviousState = (String) errorDetails.get(ERROR_DETAILS_PREVIOUS_STATE_KEY);

                if (currentAttempts > 0 && ourInProgressState.equals(currentPreviousState)) {
                  attempts = currentAttempts;
                }

                if (attempts >= taskSpec.getRetries()) {
                  value.put(STATE_KEY, taskSpec.getErrorState());
                } else {
                  value.put(STATE_KEY, taskSpec.getStartState());
                }

                value.put(STATE_CHANGED_KEY, ServerValue.TIMESTAMP);
                value.put(OWNER_KEY, null);

                errorDetails.put(ERROR_DETAILS_PREVIOUS_STATE_KEY, ourInProgressState);
                errorDetails.put(ERROR_KEY, errorMessage);

                errorDetails.put(ERROR_DETAILS_ATTEMPTS_KEY, attempts + 1);

                value.put(ERROR_DETAILS_KEY, errorDetails);
                task.setValue(value);
                return Transaction.success(task);
              } else {
                if (!ownersMatch) {
                  logger.debug("Tried aborting task " + taskRef.getKey() + " on " + ownerId + " but it is owned by " + taskOwner);
                } else {
                  logger.debug("Tried aborting task " + taskRef.getKey() + " on " + ownerId + " but its _state (" + taskState + ") did not match our _in_progress_state (" + ourInProgressState + ")");
                }

                return Transaction.abort();
              }
            }

            @Override
            public void onComplete(FirebaseError error, boolean committed, DataSnapshot snapshot) {
              final String taskKey = snapshot.getKey();

              if (error != null) {
                final long incrementedRetries = retries + 1;
                if (incrementedRetries < Queue.MAX_TRANSACTION_RETRIES) {
                  logger.debug("Received error while aborting task " + taskKey + " on " + ownerId + "...retrying", error);
                } else {
                  logger.debug("Can't aborting task " + taskKey + " on " + ownerId + " - transaction errored too many times, no longer retrying", error);
                  if (listener != null)
                    listener.onFailure("Can't aborting task - transaction errored too many times, no longer retrying", true);
                }
              } else {
                onTransactionSuccess(committed, taskKey, ACTION_ABORTED, listener);
              }
            }
          }, false);
        }
      }
      else {
        cancel();

        final String action = getActionError();
        logger.debug("Couldn't abort task " + taskRef.getKey() + " on " + ownerId + " because it has already been " + action);
        if(listener != null) listener.onFailure("Couldn't abort this task because it has already been " + action, false);
      }
    }
  }

  public void resolve() {
    resolve(new HashMap<String, Object>(), null);
  }

  public void resolve(@NotNull Listener listener) {
    resolve(new HashMap<String, Object>(), listener);
  }

  public void resolve(@NotNull Map<String, Object> newTask) {
    resolve(newTask, null);
  }

  public void resolve(@NotNull final Map<String, Object> newTask, final Listener listener) {
    resolve(newTask, listener, 0);
  }

  private void resolve(@NotNull final Map<String, Object> newTask, final Listener listener, final long retries) {
    logger.debug("Attempting to resolve task " + taskRef.getKey() + " on " + ownerId);

    if(!canActionBeTakenBeforeTransaction(ACTION_RESOLVED, listener)) {
      return;
    }

    taskRef.runTransaction(new Transaction.Handler() {
      private boolean abortedTransactionBecauseOfState;

      @Override
      public Transaction.Result doTransaction(MutableData task) {
        if(!canActionBeTakenInTransaction(listener, ACTION_RESOLVED)) {
          abortedTransactionBecauseOfState = true;
          return Transaction.abort();
        }

        // if this task no longer exists
        if(task.getValue() == null) {
          logger.debug("Tried resolving task " + taskRef.getKey() + " on " + ownerId + " after someone else removed it");
          return Transaction.success(task);
        }

        @SuppressWarnings("unchecked") Map<String, Object> value = task.getValue(Map.class);
        String ourInProgressState = taskSpec.getInProgressState();
        Object taskState = value.get(STATE_KEY);
        Object taskOwner = value.get(OWNER_KEY);
        boolean ownersMatch = ownerId.equals(taskOwner);
        if((ourInProgressState == taskState || (ourInProgressState != null && ourInProgressState.equals(taskState))) && ownersMatch) {
          if(taskSpec.getFinishedState() == null) {
            task.setValue(null);
            return Transaction.success(task);
          }

          newTask.put(STATE_KEY, taskSpec.getFinishedState());
          newTask.put(STATE_CHANGED_KEY, ServerValue.TIMESTAMP);
          newTask.put(OWNER_KEY, null);
          newTask.put(ERROR_DETAILS_KEY, null);
          task.setValue(newTask);
          return Transaction.success(task);
        }
        else {
          if(!ownersMatch) {
            logger.debug("Tried resolving task " + taskRef.getKey() + " on " + ownerId + " but it is owned by " + taskOwner);
          }
          else {
            logger.debug("Tried resolving task " + taskRef.getKey() + " on " + ownerId + " but its _state (" + taskState + ") did not match our _in_progress_state (" + ourInProgressState + ")");
          }

          return Transaction.abort();
        }
      }

      @Override
      public void onComplete(FirebaseError error, boolean committed, DataSnapshot snapshot) {
        final String taskKey = snapshot.getKey();

        if(abortedTransactionBecauseOfState) {
          return;
        }

        if(error != null) {
          final long incrementedRetries = retries + 1;
          if(incrementedRetries < Queue.MAX_TRANSACTION_RETRIES) {
            logger.debug("Received onFailure while resolving task " + taskKey + " on " + ownerId + "...retrying", error);
            resolve(newTask, listener, incrementedRetries);
          }
          else {
            logger.debug("Can't resolve task " + taskKey + " on " + ownerId + " - transaction errored too many times, no longer retrying", error);
            if(listener != null) listener.onFailure("Can't resolve task - transaction errored too many times, no longer retrying", true);
          }
        }
        else {
          onTransactionSuccess(committed, taskKey, ACTION_RESOLVED, listener);
        }
      }
    }, false);
  }

  public void reject(@NotNull Throwable error) {
    this.reject(error, null);
  }

  public void reject(Throwable error, Listener listener) {
    internalReject(error, listener);
  }

  public void reject(@NotNull String error) {
    this.reject(error, null);
  }

  public void reject(@NotNull String error, Listener listener) {
    internalReject(error, listener);
  }

  private void internalReject(@NotNull final Object errorObject, final Listener listener) {
    internalReject(errorObject, listener, 0);
  }

  private void internalReject(@NotNull final Object errorObject, final Listener listener, final long retries) {
    logger.debug("Attempting to reject task " + taskRef.getKey() + " on " + ownerId);

    if(!canActionBeTakenBeforeTransaction(ACTION_REJECTED, listener)) {
      return;
    }

    final String errorMessage;
    final String errorStack;
    if(errorObject instanceof Throwable) {
      Throwable t = ((Throwable) errorObject);
      errorMessage = t.getMessage();
      if(!suppressStack) {
        errorStack = getStackTraceAsString(t);
      }
      else {
        errorStack = null;
      }
    }
    else if(errorObject instanceof String) {
      errorMessage = ((String) errorObject);
      errorStack = null;
    }
    else {
      // sanity check
      throw new IllegalArgumentException("error must be a Throwable or a String");
    }

    taskRef.runTransaction(new Transaction.Handler() {
      private boolean abortedTransactionBecauseOfState;

      @Override
      public Transaction.Result doTransaction(MutableData task) {
        if(!canActionBeTakenInTransaction(listener, ACTION_REJECTED)) {
          abortedTransactionBecauseOfState = true;
          return Transaction.abort();
        }

        // if this task no longer exists
        if(task.getValue() == null) {
          logger.debug("Tried rejecting task " + taskRef.getKey() + " on " + ownerId + " after someone else removed it");
          return Transaction.success(task);
        }

        @SuppressWarnings("unchecked") Map<String, Object> value = task.getValue(Map.class);
        String ourInProgressState = taskSpec.getInProgressState();
        Object taskState = value.get(STATE_KEY);
        Object taskOwner = value.get(OWNER_KEY);
        boolean ownersMatch = ownerId.equals(taskOwner);
        if((ourInProgressState == taskState || (ourInProgressState != null && ourInProgressState.equals(taskState))) && ownersMatch) {
          @SuppressWarnings("unchecked") Map<String, Object> errorDetails = (Map<String, Object>) value.get(ERROR_DETAILS_KEY);
          if(errorDetails == null) {
            errorDetails = new HashMap<String, Object>();
          }

          int attempts = 0;
          Integer currentAttempts = (Integer) errorDetails.get(ERROR_DETAILS_ATTEMPTS_KEY);
          if(currentAttempts == null) {
            currentAttempts = 0;
          }
          String currentPreviousState = (String) errorDetails.get(ERROR_DETAILS_PREVIOUS_STATE_KEY);

          if(currentAttempts > 0 && ourInProgressState.equals(currentPreviousState)) {
            attempts = currentAttempts;
          }

          if(attempts >= taskSpec.getRetries()) {
            value.put(STATE_KEY, taskSpec.getErrorState());
          }
          else {
            value.put(STATE_KEY, taskSpec.getStartState());
          }

          value.put(STATE_CHANGED_KEY, ServerValue.TIMESTAMP);
          value.put(OWNER_KEY, null);

          errorDetails.put(ERROR_DETAILS_PREVIOUS_STATE_KEY, ourInProgressState);
          errorDetails.put(ERROR_KEY, errorMessage);
          if(errorStack != null) {
            errorDetails.put(ERROR_STACK_KEY, errorStack);
          }
          errorDetails.put(ERROR_DETAILS_ATTEMPTS_KEY, attempts + 1);

          value.put(ERROR_DETAILS_KEY, errorDetails);
          task.setValue(value);
          return Transaction.success(task);
        }
        else {
          if(!ownersMatch) {
            logger.debug("Tried rejecting task " + taskRef.getKey() + " on " + ownerId + " but it is owned by " + taskOwner);
          }
          else {
            logger.debug("Tried rejecting task " + taskRef.getKey() + " on " + ownerId + " but its _state (" + taskState + ") did not match our _in_progress_state (" + ourInProgressState + ")");
          }

          return Transaction.abort();
        }
      }

      @Override
      public void onComplete(FirebaseError error, boolean committed, DataSnapshot snapshot) {
        final String taskKey = snapshot.getKey();

        if(abortedTransactionBecauseOfState) {
          return;
        }

        if (error != null) {
          final long incrementedRetries = retries + 1;
          if (incrementedRetries < Queue.MAX_TRANSACTION_RETRIES) {
            logger.debug("Received error while rejecting task " + taskKey + " on " + ownerId + "...retrying", error);
            internalReject(errorObject, listener, incrementedRetries);
          }
          else {
            logger.debug("Can't reject task " + taskKey + " on " + ownerId + " - transaction errored too many times, no longer retrying", error);
            if(listener != null) listener.onFailure("Can't reject task - transaction errored too many times, no longer retrying", true);
          }
        }
        else {
          onTransactionSuccess(committed, taskKey, ACTION_REJECTED, listener);
        }
      }
    }, false);
  }

  private void complete() {
    synchronized (actionLock) {
      if(canTakeAction()) {
        completed = true;

        completionLatch.countDown();
      }
    }
  }

  private void cancel() {
    cancelled = true;

    completionLatch.countDown();
  }

  private boolean canActionBeTakenBeforeTransaction(@NotNull String action, Listener listener) {
    synchronized (actionLock) {
      if(!canTakeAction()) {
        onActionCouldNotBeTaken(action, getActionError(), listener);
        return false;
      }
    }

    return isTaskStillValid(action, listener);

  }

  private boolean canActionBeTakenInTransaction(Listener listener, String action) {
    synchronized (actionLock) {
      if(!canTakeAction()) {
        onActionCouldNotBeTaken(action, getActionError(), listener);
        return false;
      }
    }

    return isTaskStillValid(action, listener);
  }

  private void onActionCouldNotBeTaken(@NotNull String action, @NotNull String actionError, Listener listener) {
    cancel();
    logger.debug("Couldn't " + action + " task " + taskRef.getKey() + " on " + ownerId + " because it has already been " + actionError);
    if(listener != null) listener.onFailure("Couldn't " + action + " this task because it has already been " + actionError, false);
  }

  private boolean isTaskStillValid(@NotNull String action, Listener listener) {
    Thread processingThread = processingThreadRef.get();
    if(processingThread == null || !validityChecker.isValid(processingThread, ownerId)) {
      cancel();
      if(listener != null) listener.onFailure("Couldn't " + action + " this task because it is owned by another worker", false);
      logger.debug("Couldn't " + action + " task " + taskRef.getKey() + " on " + ownerId + " because we no longer own it");
      return false;
    }

    return true;
  }

  private void onTransactionSuccess(boolean committed, @NotNull String taskKey, @NotNull String action, Listener listener) {
    if(committed) {
      complete();
      logger.debug("Successful " + action + " of task " + taskKey + " on " + ownerId);
      if(listener != null) listener.onSuccess();
    }
    else {
      // the owner or the inProgressState didn't match
      cancel();
      if(listener != null) listener.onFailure("Couldn't " + action + " this task because it is owned by another worker", false);
      logger.debug("Couldn't " + action + " task " + taskRef.getKey() + " on " + ownerId + " because we no longer own it");
    }
  }

  @Override
  public String toString() {
    return "Task{" +
      "data=" + data +
      ", ownerId='" + ownerId + '\'' +
      ", taskSpec=" + taskSpec +
      '}';
  }

  interface Listener {
    void onSuccess();

    void onFailure(@NotNull String error, boolean canRetry);
  }
}
