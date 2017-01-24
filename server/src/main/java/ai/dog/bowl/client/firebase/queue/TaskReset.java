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

import java.util.Map;

import javax.validation.constraints.NotNull;

/*package*/ class TaskReset {
  private static final Logger logger = LoggerFactory.getLogger(TaskReset.class);

  private volatile TaskSpec taskSpec;

  public TaskReset() {}

  public void onNewTaskSpec(final TaskSpec taskSpec) {
    this.taskSpec = taskSpec;
  }

  public void reset(@NotNull final Firebase taskRef, @NotNull final String inProgressState) {
    reset(taskRef, null, inProgressState, null, 0);
  }

  public void reset(@NotNull final Firebase taskRef, @NotNull String ownerId, @NotNull final String inProgressState) {
    reset(taskRef, ownerId, inProgressState, null, 0);
  }

  public void reset(@NotNull final Firebase taskRef, @NotNull String ownerId, @NotNull final String inProgressState, @NotNull Listener listener) {
    reset(taskRef, ownerId, inProgressState, listener, 0);
  }

  private void reset(@NotNull final Firebase taskRef, final String ownerId, @NotNull final String inProgressState, final Listener listener, final long retries) {
    taskRef.runTransaction(new Transaction.Handler() {
      @Override
      public Transaction.Result doTransaction(MutableData task) {
        if(task.getValue() == null) {
          return Transaction.success(task);
        }

        logger.debug("Resetting task " + taskRef.getKey());

        @SuppressWarnings("unchecked") Map<String, Object> value = task.getValue(Map.class);
        Object taskOwner = value.get(Task.OWNER_KEY);
        // if the ownerId is null it means that we're force resetting this task, so we allow it to go through
        boolean ownersMatch = ownerId == null || ownerId.equals(taskOwner);
        if(inProgressState.equals(value.get(Task.STATE_KEY)) && ownersMatch) {
          value.put(Task.STATE_KEY, taskSpec.getStartState());
          value.put(Task.STATE_CHANGED_KEY, ServerValue.TIMESTAMP);
          value.put(Task.OWNER_KEY, null);
          value.put(Task.ERROR_DETAILS_KEY, null);
          task.setValue(value);
          return Transaction.success(task);
        }
        else {
          if(listener != null) listener.onResetFailed("Couldn't reset this task because it is owned by another worker", false);

          if(!ownersMatch) {
            logger.debug("Can't reset task " + taskRef.getKey() + " on " + ownerId + " because it is owned by " + taskOwner);
          }
          else {
            logger.debug("Can't reset task " + taskRef.getKey() + " - _state != in_progress_state");
          }
          return Transaction.abort();
        }
      }

      @Override
      public void onComplete(FirebaseError error, boolean committed, DataSnapshot snapshot) {
        String taskKey = snapshot.getKey();
        if(error != null) {
          final long incrementedRetries = retries + 1;
          if (incrementedRetries < FirebaseQueue.MAX_TRANSACTION_RETRIES) {
            logger.debug("Received error while resetting task " + taskKey + "...retrying", error);
            reset(taskRef, ownerId, inProgressState, listener, incrementedRetries);
          }
          else {
            logger.debug("Can't reset task " + taskKey + " - transaction errored too many times, no longer retrying", error);
            if(listener != null) listener.onResetFailed("Can't reset task - transaction errored too many times, no longer retrying", true);
          }
        }
        else {
          if(committed && snapshot.exists()) {
            logger.debug("reset " + taskKey);
          }
          if(listener != null) listener.onReset();
        }
      }
    }, false);
  }

  public interface Listener {
    void onReset();

    void onResetFailed(String error, boolean canRetry);
  }
}
