/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import ai.dog.bowl.repository.StatsRepository;

import static com.google.common.collect.ImmutableMap.of;
import static java.time.ZonedDateTime.parse;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.slf4j.LoggerFactory.getLogger;

public class StatsFirebaseRestRepository extends FirebaseRestRepository implements StatsRepository {
  private static final org.slf4j.Logger logger = getLogger(StatsFirebaseRestRepository.class);

  @Override
  public ZonedDateTime retrieveAllTimePeriodEndDate(String companyId, String employeeId, String performanceName) {
    String path = "company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName + "/_stats/period_end_date";

    String value = firebase.getValueAsString(path);

    if (value == null || "null".equals(value)) {
      return null;
    }

    ZonedDateTime date = parse(value.replace("\"", ""), ISO_ZONED_DATE_TIME);

    return date;
  }

  @Override
  public ZonedDateTime retrieveAllTimeUpdatedDate(String companyId, String employeeId, String performanceName) {
    String path = "company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName + "/_stats/updated_date";

    String value = firebase.getValueAsString(path);

    if (value == null || "null".equals(value)) {
      return null;
    }

    ZonedDateTime date = parse(value.replace("\"", ""), ISO_ZONED_DATE_TIME);

    return date;
  }

  @Override
  public Map<String, Object> retrieve(String companyId, String employeeId, String performanceName, String period, ZonedDateTime date) {
    logger.debug("Started retrieve employee day performance: " + companyId + ", " + employeeId + ", " + performanceName + ", " + period + ", " + date.format(ISO_ZONED_DATE_TIME));

    StringBuilder builder = new StringBuilder();
    builder.append("company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName + "/");

    switch(period) {
      case "day":
        builder.append(date.format(ofPattern("yyyy/MM/dd")) + "/_stats");
        break;
      case "month":
        builder.append(date.format(ofPattern("yyyy/MM")) + "/_stats");
        break;
      case "year":
        builder.append(date.format(ofPattern("yyyy")) + "/_stats");
        break;
      case "all-time":
        builder.append("_stats");
        break;
      default:
        throw new IllegalArgumentException(period);
    }

    String path = builder.toString();

    Map value = firebase.getValueAsMap(path);

    if (value == null || "null".equals(null)) {
      return null;
    }

    // TODO: temp. fix
    if (value.get("maximum_end_time") instanceof Double) {
      value.put("maximum_end_time", ((Double) value.get("maximum_end_time")).intValue());
    }
    if (value.get("maximum_start_time") instanceof Double) {
      value.put("maximum_start_time", ((Double) value.get("maximum_start_time")).intValue());
    }
    if (value.get("maximum_total_duration") instanceof Double) {
      value.put("maximum_total_duration", ((Double) value.get("maximum_total_duration")).intValue());
    }
    if (value.get("minimum_end_time") instanceof Double) {
      value.put("minimum_end_time", ((Double) value.get("minimum_end_time")).intValue());
    }
    if (value.get("minimum_start_time") instanceof Double) {
      value.put("minimum_start_time", ((Double) value.get("minimum_start_time")).intValue());
    }
    if (value.get("minimum_total_duration") instanceof Double) {
      value.put("minimum_total_duration", ((Double) value.get("minimum_total_duration")).intValue());
    }
    if (value.get("average_end_time") instanceof Integer) {
      value.put("average_end_time", ((Integer) value.get("average_end_time")).doubleValue());
    }
    if (value.get("average_start_time") instanceof Integer) {
      value.put("average_start_time", ((Integer) value.get("average_start_time")).doubleValue());
    }
    if (value.get("average_total_duration") instanceof Integer) {
      value.put("average_total_duration", ((Integer) value.get("average_total_duration")).doubleValue());
    }
    if (value.get("previous_average_end_time") instanceof Integer) {
      value.put("previous_average_end_time", ((Integer) value.get("previous_average_end_time")).doubleValue());
    }
    if (value.get("previous_average_start_time") instanceof Integer) {
      value.put("previous_average_start_time", ((Integer) value.get("previous_average_start_time")).doubleValue());
    }
    if (value.get("previous_average_total_duration") instanceof Integer) {
      value.put("previous_average_total_duration", ((Integer) value.get("previous_average_total_duration")).doubleValue());
    }

    return value;
  }

  @Override
  public void update(String companyId, String employeeId, String performanceName, String period, ZonedDateTime date, Map<String, Object> stats) {
    logger.debug("Started update employee period performance stats: " + companyId + ", " + employeeId + ", " + performanceName + ", " + period + ", " + date.format(ISO_ZONED_DATE_TIME) + ", " + stats);

    StringBuilder builder = new StringBuilder();
    builder.append("company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName + "/");

    switch (period) {
      case "day":
        builder.append(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/_stats");
        break;
      case "month":
        builder.append(date.format(DateTimeFormatter.ofPattern("yyyy/MM")) + "/_stats");
        break;
      case "year":
        builder.append(date.format(DateTimeFormatter.ofPattern("yyyy")) + "/_stats");
        break;
      case "all-time":
        builder.append("_stats");
        break;
      default:
        throw new IllegalArgumentException(period);
    }

    String path = builder.toString();

    firebase.setValue(path, stats);
  }

  @Override
  public void updatePeriodEndDate(String companyId, String employeeId, String performanceName, String period, ZonedDateTime periodEndDate) {
    logger.debug("Started updateValue employee period performance stats period end date: " + companyId + ", " + employeeId + ", " + performanceName + ", " + period + ", " + periodEndDate.format(ISO_ZONED_DATE_TIME));

    StringBuilder builder = new StringBuilder();
    builder.append("company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName + "/");

    switch (period) {
      case "day":
        builder.append(periodEndDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/_stats");
        break;
      case "month":
        builder.append(periodEndDate.format(DateTimeFormatter.ofPattern("yyyy/MM")) + "/_stats");
        break;
      case "year":
        builder.append(periodEndDate.format(DateTimeFormatter.ofPattern("yyyy")) + "/_stats");
        break;
      case "all-time":
        builder.append("_stats");
        break;
      default:
        throw new IllegalArgumentException(period);
    }

    String path = builder.toString();

    firebase.updateValue(path, of("period_end_date", periodEndDate.format(ISO_ZONED_DATE_TIME)));
  }

  @Override
  public void deleteAll(String companyId, String employeeId, String performanceName) {
    String path = "company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName;

    Map<String, Object> value = firebase.getValueAsMap(path, true);

    if (value == null || "null".equals(value)) {
      return;
    }

    for (String key : value.keySet()) {
      String _path = path + "/" + key;

      // all-time
      if ("_stats".equals(key)) {
        firebase.deleteValue(_path);

        continue;
      }

      Map<String, Object> _value = firebase.getValueAsMap(_path, true);

      for (String _key : _value.keySet()) {
        String __path = _path + "/" + _key;

        // year
        if ("_stats".equals(_key)) {
          firebase.deleteValue(__path);

          continue;
        }

        Map<String, Object> __value = firebase.getValueAsMap(__path, true);

        for (String __key : __value.keySet()) {
          String ___path = __path + "/" + __key;

          // month
          if ("_stats".equals(__key)) {
            firebase.deleteValue(___path);

            continue;
          }

          Map<String, Object> ___value = firebase.getValueAsMap(___path, true);

          for (String ___key : ___value.keySet()) {
            String ____path = ___path + "/" + ___key;

            // day
            if ("_stats".equals(___key)) {
              firebase.deleteValue(____path);

              continue;
            }
          }
        }
      }
    }
  }
}
