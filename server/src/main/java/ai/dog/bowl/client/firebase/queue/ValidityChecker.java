/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.firebase.queue;

import java.util.HashMap;

import javax.validation.constraints.NotNull;

/*package*/ class ValidityChecker {
  private static final HashMap<Long, String> threadIdToTaskIdMap = new HashMap<Long, String>();

  private final Long id;

  public ValidityChecker(@NotNull Thread thread, @NotNull String taskId) {
    this.id = thread.getId();

    threadIdToTaskIdMap.put(id, taskId);
  }

  public boolean isValid(@NotNull Thread processingThread, @NotNull String taskId) {
    return taskId.equals(threadIdToTaskIdMap.get(processingThread.getId()));
  }

  public void destroy() {
    threadIdToTaskIdMap.remove(id);
  }
}
