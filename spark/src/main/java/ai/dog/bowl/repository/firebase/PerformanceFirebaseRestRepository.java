/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.SortedMap;

import ai.dog.bowl.repository.PerformanceRepository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.slf4j.LoggerFactory.getLogger;

public class PerformanceFirebaseRestRepository extends FirebaseRestRepository implements PerformanceRepository {
  private static final org.slf4j.Logger logger = getLogger(PerformanceFirebaseRestRepository.class);

  @Override
  public Map<String, Map> findAllByNameAndDate(String companyId, String employeeId, String performanceName, Instant date) {
    checkArgument(!isNullOrEmpty(companyId));
    checkArgument(!isNullOrEmpty(employeeId));
    checkArgument(!isNullOrEmpty(performanceName));
    checkNotNull(date);

    logger.debug("Started retrieve employee day performance: " + companyId + ", " + employeeId + ", " + performanceName + ", " + date);

    String path = "company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName + "/" + ofPattern("yyyy/MM/dd").withZone(ZoneId.of("Z")).format(date);

    Map value = client.getValueAsMap(path);

    if (value == null) {
      return null;
    }

    if (value.containsKey("_stats")) {
      value.remove("_stats");
    }

    /*
    for (String key : (Set<String>) value.keySet()) {
      Integer createdDate = (Integer) ((Map<String, Object>) value.get(key)).get("created_date");
      ((Map<String, Object>) value.get(key)).put("created_date", createdDate.longValue());
    }*/

    return value;
  }

  @Override
  public Instant findFirstPerformanceDate(String companyId, String employeeId, String performanceName) {
    checkArgument(!isNullOrEmpty(companyId));
    checkArgument(!isNullOrEmpty(employeeId));
    checkArgument(!isNullOrEmpty(performanceName));

    String path = "company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName;

    SortedMap<String, Object> value = (SortedMap<String, Object>) client.getValueAsMap(path, true);

    if (value == null || "null".equals(value)) {
      return null;
    }

    int year = Integer.parseInt(value.firstKey());
    path += "/" + value.firstKey();

    value = (SortedMap<String, Object>) client.getValueAsMap(path, true);

    if (value == null || "null".equals(value)) {
      return null;
    }

    int month = Integer.parseInt(value.firstKey());
    path += "/" + value.firstKey();

    value = (SortedMap<String, Object>) client.getValueAsMap(path, true);

    if (value == null || "null".equals(value)) {
      return null;
    }

    int day = Integer.parseInt(value.firstKey());

    ZonedDateTime date = ZonedDateTime.parse(year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day) + "T00:00:00Z");

    return date.toLocalDate().atStartOfDay().atZone(ZoneId.of("Z")).toInstant();
  }
}
