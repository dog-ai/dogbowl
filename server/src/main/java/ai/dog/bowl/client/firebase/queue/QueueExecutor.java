/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.firebase.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

public class QueueExecutor extends ThreadPoolExecutor {
  private static final Logger logger = LoggerFactory.getLogger(QueueExecutor.class);

  private FirebaseQueue.TaskStateListener taskStateListener;

  public QueueExecutor(int numWorkers) {
    this(numWorkers, numWorkers, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
  }

  public QueueExecutor(int corePoolSize, int maximumPoolSize) {
    this(corePoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
  }

  public QueueExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>());
  }

  public QueueExecutor(int corePoolSize, int maximumPoolSize, BlockingQueue<Runnable> workQueue) {
    this(corePoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS, workQueue);
  }

  public QueueExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new ThreadFactory(), new RejectedExecutionHandler());
  }

  /*package*/ void setTaskStateListener(FirebaseQueue.TaskStateListener taskStateListener) {
    this.taskStateListener = taskStateListener;
  }

  @Override
  public void setThreadFactory(java.util.concurrent.ThreadFactory threadFactory) {
    throw new UnsupportedOperationException("The ThreadFactory of a QueueExecutor cannot be changed");
  }

  @Override
  public void setRejectedExecutionHandler(java.util.concurrent.RejectedExecutionHandler handler) {
    throw new UnsupportedOperationException("The RejectedExecutionHandler of a QueueExecutor cannot be changed");
  }

  @Override
  protected final void beforeExecute(Thread t, Runnable r) {
    if(r instanceof QueueTask) {
      if(taskStateListener != null) taskStateListener.onTaskStart(t, (QueueTask) r);
    }
  }

  @Override
  protected final void afterExecute(Runnable r, Throwable t) {
    if(r instanceof QueueTask) {
      if(taskStateListener != null) taskStateListener.onTaskFinished((QueueTask) r, t);
    }
  }

  public interface Factory {
    QueueExecutor get();
  }

  private static final class ThreadFactory implements java.util.concurrent.ThreadFactory {
    @Override
    public Thread newThread(@NotNull Runnable r) {
      Thread t = new Thread(r);
      t.setName(UUID.randomUUID().toString());
      return t;
    }
  }

  private static final class RejectedExecutionHandler implements java.util.concurrent.RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
      logger.debug("Task " + r.toString() + " rejected from " + executor.toString());
    }
  }
}
