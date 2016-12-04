/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository;

import java.time.ZonedDateTime;
import java.util.Map;

public interface StatsRepository {
  ZonedDateTime retrieveAllTimePeriodEndDate(String companyId, String employeeId, String performanceName);

  Map<String, Object> retrieve(String companyId, String employeeId, String performanceName, String period, ZonedDateTime date);

  void update(String companyId, String employeeId, String performanceName, String period, ZonedDateTime date, Map<String, Object> stats);

  void updatePeriodEndDate(String companyId, String employeeId, String performanceName, String period, ZonedDateTime periodEndDate);

  void deleteAll(String companyId, String employeeId, String performanceName);
}
