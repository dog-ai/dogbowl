/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;

import ai.dog.bowl.repository.PerformanceRepository;

import static java.time.ZonedDateTime.parse;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.slf4j.LoggerFactory.getLogger;

public class PerformanceFirebaseRestRepository extends FirebaseRepository implements PerformanceRepository {
  private static final org.slf4j.Logger logger = getLogger(PerformanceFirebaseRestRepository.class);

  @Override
  public Map<String, Map> findAllByNameAndDate(String companyId, String employeeId, String performanceName, ZonedDateTime date) {
    logger.debug("Started retrieve employee day performance: " + companyId + ", " + employeeId + ", " + performanceName + ", " + date.format(ISO_ZONED_DATE_TIME));

    String path = "company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName + "/" + date.format(ofPattern("yyyy/MM/dd"));

    Map value = firebase.getValueAsMap(path);

    if (value == null) {
      return null;
    }

    if (value.containsKey("_stats")) {
      value.remove("_stats");
    }

    return value;
  }

  @Override
  public ZonedDateTime findFirstPerformanceDate(String companyId, String employeeId, String performanceName, String timezone) {
    String path = "company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName;

    SortedMap<String, Object> value = (SortedMap<String, Object>) firebase.getValueAsMap(path, true);

    if (value == null || "null".equals(value)) {
      return null;
    }

    int year = Integer.parseInt(value.firstKey());
    path += "/" + value.firstKey();

    value = (SortedMap<String, Object>) firebase.getValueAsMap(path, true);

    if (value == null || "null".equals(value)) {
      return null;
    }

    int month = Integer.parseInt(value.firstKey());
    path += "/" + value.firstKey();

    value = (SortedMap<String, Object>) firebase.getValueAsMap(path, true);

    if (value == null || "null".equals(value)) {
      return null;
    }

    int day = Integer.parseInt(value.firstKey());

    Calendar calendar = new GregorianCalendar(year, month - 1, day, 23, 59, 59);
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    format.setTimeZone(TimeZone.getTimeZone(timezone));

    ZonedDateTime date = parse(format.format(new Date(calendar.getTimeInMillis())), ISO_ZONED_DATE_TIME);

    return date;
  }
}
