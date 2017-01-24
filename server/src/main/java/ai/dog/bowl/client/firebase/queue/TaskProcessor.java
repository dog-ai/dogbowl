/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.firebase.queue;

public interface TaskProcessor {
  void process(Task task) throws InterruptedException;
}
