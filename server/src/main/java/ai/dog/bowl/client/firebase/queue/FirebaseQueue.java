/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.firebase.queue;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.validation.constraints.NotNull;

public class FirebaseQueue {
  public static final long MAX_TRANSACTION_RETRIES = 10;
  private static final Logger logger = LoggerFactory.getLogger(FirebaseQueue.class);
  private static final TaskSpec DEFAULT_TASK_SPEC = new TaskSpec();
  static boolean slept;
  static boolean aborted;
  private final Options options;

  private final Map<String, QueueTask> executingTasks = new HashMap<String, QueueTask>();
  private final Map<String, Runnable> timeoutsInFlight = new HashMap<String, Runnable>();

  private final QueueExecutor.Factory executorFactory;
  private final Firebase taskRef;
  private final TaskReset taskReset;
  private final Firebase specRef;
  private final TaskStateListener taskStateListener = new TaskStateListener() {
    @Override
    public void onTaskStart(Thread thread, QueueTask task) {
      logger.info("Started task with id " + task.getTaskKey());

      executingTasks.put(task.getTaskKey(), task);
    }

    @Override
    public void onTaskFinished(QueueTask task, Throwable error) {
      executingTasks.remove(task.getTaskKey());

      logger.info("Finished task with id " + task.getTaskKey());
    }
  };
  private QueueExecutor executor;
  private ScheduledThreadPoolExecutor timeoutExecutor;
  private Query newTaskQuery;
  private Query timeoutQuery;
  private TaskSpec taskSpec;
  private AtomicBoolean shutdown = new AtomicBoolean(false);
  private final ChildEventListener newTaskListener = new ChildEventAdapter() {
    @Override
    public void onChildAdded(final DataSnapshot taskSnapshot, String previousChildKey) {
      onNewTask(taskSnapshot);
    }

    @Override
    public void onChildChanged(DataSnapshot taskSnapshot, String previousChildKey) {
      onNewTask(taskSnapshot);
    }

    private void onNewTask(DataSnapshot taskSnapshot) {
      if(shutdown.get()) {
        return;
      }

      logger.info("Received new task with id " + taskSnapshot.getKey());

      QueueTask task = new QueueTask(taskSnapshot.getRef(), taskSpec, taskReset, options);
      executor.execute(task);
    }

    @Override
    public void onCancelled(FirebaseError error) {
      logger.error("There was an error listening for children with a " + Task.STATE_KEY + " of " + taskSpec.getStartState(), error);
    }
  };
  private final ChildEventListener timeoutListener = new ChildEventAdapter() {
    @Override
    public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
      setTimeout(snapshot);
    }

    @Override
    public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {
      setTimeout(snapshot);
    }

    @Override
    public void onChildRemoved(DataSnapshot snapshot) {
      Runnable timeout = timeoutsInFlight.remove(snapshot.getKey());
      if(timeout != null) {
        timeoutExecutor.remove(timeout);
        logger.debug("Cancelling timeout for " + snapshot);
      }
    }

    private void setTimeout(final DataSnapshot snapshot) {
      if(shutdown.get()) {
        return;
      }

      long timeoutDelay = getTimeoutDelay(snapshot);

      Runnable timeout = new Runnable() {
        @Override
        public void run() {
          if(timeoutsInFlight.remove(snapshot.getKey()) == null) {
            return;
          }

          QueueTask runningTask = executingTasks.remove(snapshot.getKey());
          if(runningTask != null) {
            logger.warn("Task " + runningTask.getTaskKey() + " has timedout while running");
            runningTask.cancel("timeout");
          }
          else {
            logger.warn("Task " + snapshot.getKey() + " has timedout");
          }

          //taskReset.reset(snapshot.getRef(), taskSpec.getInProgressState());
        }
      };

      if(timeoutsInFlight.containsKey(snapshot.getKey())) {
        timeoutExecutor.remove(timeoutsInFlight.get(snapshot.getKey()));
        logger.debug("Received updated task to monitor for timeouts - " + snapshot + " (timeout in " + timeoutDelay + " ms)");
      }
      else {
        logger.debug("Received new task to monitor for timeouts - " + snapshot + " (timeout in " + timeoutDelay + " ms)");
      }

      timeoutExecutor.schedule(timeout, timeoutDelay, TimeUnit.MILLISECONDS);
      timeoutsInFlight.put(snapshot.getKey(), timeout);
    }

    private long getTimeoutDelay(DataSnapshot snapshot) {
      @SuppressWarnings("unchecked") Map<String, Object> value = snapshot.getValue(Map.class);
      Object timeStartedVal = value.get(Task.STATE_CHANGED_KEY);
      if(timeStartedVal instanceof Long) {
        long timeStarted = (Long) timeStartedVal;
        long timeElapsedSinceStart = (System.currentTimeMillis() - timeStarted);
        long timeout = taskSpec.getTimeout() - (timeElapsedSinceStart < 0 ? 0 : timeElapsedSinceStart);
        return timeout < 0 ? 0 : timeout;
      }
      else {
        return 0;
      }
    }

    @Override
    public void onCancelled(FirebaseError error) {
      logger.error("There was an error listening for timeouts with a " + Task.STATE_KEY + " of " + taskSpec.getInProgressState(), error);
    }
  };
  private final ValueEventListener specChangeListener = new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot specSnapshot) {
      if(shutdown.get()) {
        return;
      }

      taskSpec = new TaskSpec(specSnapshot);
      if(taskSpec.validate()) {
        logger.debug("Got a new spec - " + taskSpec);
        onNewSpec();
      }
      else {
        logger.warn("Got a new spec, but it was not valid - " + taskSpec);
        onInvalidSpec();
        taskSpec = null;
      }
    }

    @Override
    public void onCancelled(FirebaseError error) {
      logger.error("There was an error listening for value events on the spec child", error);
    }
  };

  private FirebaseQueue(Builder builder) {
    this.options = new Options(builder);

    taskRef = builder.firebase.child(builder.taskChild);

    executorFactory = builder.executorFactory;

    taskReset = new TaskReset();

    if(options.specId == null) {
      specRef = null;
      taskSpec = DEFAULT_TASK_SPEC;
      onNewSpec();
    }
    else {
      specRef = builder.firebase.child(builder.specChild);
      specRef.child(options.specId).addValueEventListener(specChangeListener);
    }
  }

  public void shutdown() {
    if(!shutdown.getAndSet(true)) {
      specRef.removeEventListener(specChangeListener);
      stopListeningForNewTasks();
      shutdownExecutors();
    }
  }

  private void onNewSpec() {
    taskReset.onNewTaskSpec(taskSpec);

    stopListeningForNewTasks();

    shutdownExecutors();

    startExecutors();

    startListeningForNewTasks();
  }

  private void onInvalidSpec() {
    stopListeningForNewTasks();

    shutdownExecutors();
  }

  private void startExecutors() {
    if(shutdown.get()) {
      return;
    }

    executor = executorFactory.get();
    executor.setTaskStateListener(taskStateListener);
    timeoutExecutor = new ScheduledThreadPoolExecutor(1);
  }

  /**
   * shutting down the executors will implicitly cancel all of their running tasks and not run any pending tasks
   */
  private void shutdownExecutors() {
    if(executor != null) {
      executor.shutdownNow();
      executor.setTaskStateListener(null);
      executor = null;

      executingTasks.clear();
    }

    if(timeoutExecutor != null) {
      timeoutExecutor.shutdownNow();
      timeoutExecutor = null;

      timeoutsInFlight.clear();
    }
  }

  private void startListeningForNewTasks() {
    if(shutdown.get()) {
      return;
    }

    newTaskQuery = taskRef.orderByChild(Task.STATE_KEY).equalTo(taskSpec.getStartState()).limitToFirst(1);
    newTaskQuery.addChildEventListener(newTaskListener);

    timeoutQuery = taskRef.orderByChild(Task.STATE_KEY).equalTo(taskSpec.getInProgressState());
    timeoutQuery.addChildEventListener(timeoutListener);
  }

  private void stopListeningForNewTasks() {
    if(newTaskQuery != null && newTaskListener != null) {
      newTaskQuery.removeEventListener(newTaskListener);
    }

    if(timeoutQuery != null && timeoutListener != null) {
      timeoutQuery.removeEventListener(timeoutListener);
    }
  }

  public interface TaskStateListener {
    void onTaskStart(Thread thread, QueueTask task);

    void onTaskFinished(QueueTask task, Throwable error);
  }

  public static class Builder {
    private static final String DEFAULT_TASK_CHILD = "tasks";
    private static final String DEFAULT_SPEC_CHILD = "specs";
    private static final String DEFAULT_SPEC_ID = null;
    private static final int DEFAULT_NUM_WORKERS = 1;
    private static final int UNINITIALIZED_NUM_WORKERS = 1;
    private static final boolean DEFAULT_SANITIZE = true;
    private static final boolean DEFAULT_SUPPRESS_STACK = false;

    private boolean built;

    private Firebase firebase;
    private TaskProcessor taskProcessor;

    private String taskChild = DEFAULT_TASK_CHILD;
    private String specChild = DEFAULT_SPEC_CHILD;
    private String specId = DEFAULT_SPEC_ID;
    private int numWorkers = UNINITIALIZED_NUM_WORKERS;
    private boolean sanitize = DEFAULT_SANITIZE;
    private boolean suppressStack = DEFAULT_SUPPRESS_STACK;

    private QueueExecutor.Factory executorFactory;

    public Builder(@NotNull Firebase firebase, @NotNull TaskProcessor taskProcessor) {
      this.firebase = firebase;
      this.taskProcessor = taskProcessor;
    }

    public Builder taskChild(String taskChild) {
      this.taskChild = taskChild;
      return this;
    }

    public Builder specChild(String specChild) {
      this.specChild = specChild;
      return this;
    }

    public Builder specId(String specId) {
      this.specId = specId;
      return this;
    }

    /**
     * @throws IllegalArgumentException if {@link Builder#executorFactory(QueueExecutor.Factory)} was called with a non-{@code null} value,
     * or if {@code numWorkers} is < 1
     */
    public Builder numWorkers(int numWorkers) {
      if(executorFactory != null) {
        throw new IllegalArgumentException("Cannot set numWorkers if executorFactory has been set to a non-null value");
      }
      else if(numWorkers < 1) {
        throw new IllegalArgumentException("numWorkers must be greater than 0");
      }

      this.numWorkers = numWorkers;
      return this;
    }

    public Builder sanitize(boolean sanitize) {
      this.sanitize = sanitize;
      return this;
    }

    public Builder suppressStack(boolean suppressStack) {
      this.suppressStack = suppressStack;
      return this;
    }

    /**
     * @throws IllegalArgumentException if {@link Builder#numWorkers(int)} was called
     */
    public Builder executorFactory(QueueExecutor.Factory executorFactory) {
      if(numWorkers != UNINITIALIZED_NUM_WORKERS) {
        throw new IllegalArgumentException("Cannot set executorFactory if numWorkers has been set");
      }

      this.executorFactory = executorFactory;
      return this;
    }

    public FirebaseQueue build() {
      if(built) {
        throw new IllegalStateException("Cannot call build twice");
      }
      built = true;

      if(numWorkers == UNINITIALIZED_NUM_WORKERS) {
        numWorkers = DEFAULT_NUM_WORKERS;
      }
      if(executorFactory == null) {
        executorFactory = new QueueExecutor.Factory() {
          @Override
          public QueueExecutor get() {
            return new QueueExecutor(numWorkers);
          }
        };
      }

      return new FirebaseQueue(this);
    }
  }

  /*package*/ static class Options {
    public final String specId;
    public final int numWorkers;
    public final boolean sanitize;
    public final boolean suppressStack;
    public final TaskProcessor taskProcessor;

    public Options(Builder builder) {
      this.specId = builder.specId;
      this.numWorkers = builder.numWorkers;
      this.sanitize = builder.sanitize;
      this.suppressStack = builder.suppressStack;
      this.taskProcessor = builder.taskProcessor;
    }
  }
}
