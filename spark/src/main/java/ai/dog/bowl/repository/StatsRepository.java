/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository;

import java.time.Instant;
import java.util.Map;

public interface StatsRepository {
  Instant retrieveAllTimePeriodEndDate(String companyId, String employeeId, String performanceName);

  Instant retrieveAllTimeUpdatedDate(String companyId, String employeeId, String performanceName);

  Map<String, Object> retrieve(String companyId, String employeeId, String performanceName, String period, Instant date);

  void update(String companyId, String employeeId, String performanceName, String period, Instant date, Map<String, Object> stats);

  void updatePeriodEndDate(String companyId, String employeeId, String performanceName, String period, Instant date, Long periodEndDate);

  void deleteAll(String companyId, String employeeId, String performanceName);
}
