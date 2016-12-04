/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository;

import java.time.ZonedDateTime;
import java.util.Map;

public interface PerformanceRepository {
  Map<String, Map> findAllByNameAndDate(String companyId, String employeeId, String performanceName, ZonedDateTime date);

  ZonedDateTime findFirstPerformanceDate(String companyId, String employeeId, String performanceName, String timezone);
}
