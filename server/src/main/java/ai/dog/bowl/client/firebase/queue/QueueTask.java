/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.firebase.queue;

import com.firebase.client.Firebase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/*package*/ class QueueTask implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(QueueTask.class);
  private final Firebase taskRef;
  private final TaskSpec taskSpec;
  private final TaskReset taskReset;
  private final FirebaseQueue.Options options;
  private String id;
  private Thread executingThread;
  private Task task;

  private volatile boolean claimed;
  private volatile boolean cancelled;
  private volatile boolean done;

  public QueueTask(Firebase taskRef, TaskSpec taskSpec, TaskReset taskReset, FirebaseQueue.Options options) {
    this.taskRef = taskRef;
    this.taskSpec = taskSpec;
    this.taskReset = taskReset;
    this.options = options;
  }

  public String getId() {
    return id;
  }

  public String getTaskKey() {
    return taskRef.getKey();
  }

  public boolean isClaimed() {
    return claimed;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public boolean isDone() {
    return done;
  }

  public void cancel(String reason) {
    String id = this.id == null ? "<id not set yet>" : this.id;

    if(cancelled || done) {
      logger.debug("Not cancelling task (" + taskRef.getKey() + ") on " + id + " because it " + (cancelled ? "was already cancelled" : " is already done"));
    }
    else {
      cancelled = true;

      if(executingThread == null) {
        logger.debug("Delaying cancelling task (" + taskRef.getKey() + ") on " + id + " because it hasn't started running yet");
      }
      else {
        logger.debug("Cancelling task (" + taskRef.getKey() + ") on " + id);
        task.abort(reason, false);
      }
    }
  }

  @Override
  public void run() {
    id = Thread.currentThread().getName() + ":" + UUID.randomUUID().toString();

    if(cancelled) {
      logger.debug("Can't run task (" + taskRef.getKey() + ") on " + id +  " because it has previously been cancelled");
      return;
    }

    executingThread = Thread.currentThread();

    logger.debug("Started claiming task (" + taskRef.getKey() + ") on " + id);

    TaskClaimer.TaskGenerator taskGenerator = new TaskClaimer(id, taskRef, taskSpec, taskReset, options.sanitize).claimTask();
    if(taskGenerator == null) {
      logger.debug("Couldn't claim task (" + taskRef.getKey() + ") on " + id);
      done = true;
      return;
    }

    logger.debug("Claimed task (" + taskRef.getKey() + ") on " + id);

    // it is possible that we got cancelled while claiming a task and the TaskClaimer didn't pick that up
    if(cancelled) {
      logger.debug("Can't process task (" + taskRef.getKey() + ") on " + id + " because it was cancelled while we were claiming it");
      done = true;
      return;
    }

    claimed = true;

    logger.debug("Started processing task with id " + taskRef.getKey() + " on " + id);

    ValidityChecker validityChecker = new ValidityChecker(Thread.currentThread(), id);

    task = taskGenerator.generateTask(id, taskSpec, taskReset, validityChecker, options);
    task.process(options.taskProcessor);

    done = true;

    validityChecker.destroy();

    logger.debug("Finished processing task with id " + taskRef.getKey() + " on " + id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    QueueTask queueTask = (QueueTask) o;

    return id.equals(queueTask.id);

  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return "QueueTask{" +
            "id='" + id + "', " +
            "task='" + taskRef.getKey() + '\'' +
            '}';
  }
}
