/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository;

import java.time.Instant;
import java.util.List;

import ai.dog.bowl.model.performance.Presence;

public interface PresencePerformanceRepository {
  List<Presence> findAllByDate(String companyId, String employeeId, Instant date);

  Instant findFirstPerformanceDate(String companyId, String employeeId);
}
