/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository;

import java.time.Instant;
import java.util.Map;

public interface PerformanceRepository {
  Map<String, Map> findAllByNameAndDate(String companyId, String employeeId, String performanceName, Instant date);

  Instant findFirstPerformanceDate(String companyId, String employeeId, String performanceName);
}
