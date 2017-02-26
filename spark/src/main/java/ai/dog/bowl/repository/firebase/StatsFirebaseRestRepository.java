/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import ai.dog.bowl.repository.StatsRepository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableMap.of;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.slf4j.LoggerFactory.getLogger;

public class StatsFirebaseRestRepository extends FirebaseRestRepository implements StatsRepository {
  private static final org.slf4j.Logger logger = getLogger(StatsFirebaseRestRepository.class);

  @Override
  public Instant retrieveAllTimePeriodEndDate(String companyId, String employeeId, String performanceName) {
    checkArgument(!isNullOrEmpty(companyId));
    checkArgument(!isNullOrEmpty(employeeId));
    checkArgument(!isNullOrEmpty(performanceName));

    String path = "company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName + "/_stats/period_end_date";

    Long value = client.getValueAsLong(path);

    if (value == null) {
      return null;
    }

    Instant date = Instant.ofEpochSecond(value);

    return date;
  }

  @Override
  public Instant retrieveAllTimeUpdatedDate(String companyId, String employeeId, String performanceName) {
    checkArgument(!isNullOrEmpty(companyId));
    checkArgument(!isNullOrEmpty(employeeId));
    checkArgument(!isNullOrEmpty(performanceName));

    String path = "company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName + "/_stats/updated_date";

    Long value = client.getValueAsLong(path);

    if (value == null) {
      return null;
    }

    Instant date = Instant.ofEpochSecond(value);

    return date;
  }

  @Override
  public Map<String, Object> retrieve(String companyId, String employeeId, String performanceName, String period, Instant date) {
    checkArgument(!isNullOrEmpty(companyId));
    checkArgument(!isNullOrEmpty(employeeId));
    checkArgument(!isNullOrEmpty(performanceName));
    checkArgument(!isNullOrEmpty(period));
    checkNotNull(date);

    logger.debug("Started retrieve employee day performance: " + companyId + ", " + employeeId + ", " + performanceName + ", " + period + ", " + date);

    StringBuilder builder = new StringBuilder();
    builder.append("company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName + "/");

    switch(period) {
      case "day":
        builder.append(ofPattern("yyyy/MM/dd").withZone(ZoneId.of("Z")).format(date) + "/_stats");
        break;
      case "month":
        builder.append(ofPattern("yyyy/MM").withZone(ZoneId.of("Z")).format(date) + "/_stats");
        break;
      case "year":
        builder.append(ofPattern("yyyy").withZone(ZoneId.of("Z")).format(date) + "/_stats");
        break;
      case "all-time":
        builder.append("_stats");
        break;
      default:
        throw new IllegalArgumentException(period);
    }

    String path = builder.toString();

    Map value = client.getValueAsMap(path);

    if (value == null || "null".equals(null)) {
      return null;
    }

    // TODO: temp. fix
    if (value.get("period_start_date") instanceof Integer) {
      value.put("period_start_date", ((Integer) value.get("period_start_date")).longValue());
    }
    if (value.get("period_end_date") instanceof Integer) {
      value.put("period_end_date", ((Integer) value.get("period_end_date")).longValue());
    }
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
  public void update(String companyId, String employeeId, String performanceName, String period, Instant date, Map<String, Object> stats) {
    checkArgument(!isNullOrEmpty(companyId));
    checkArgument(!isNullOrEmpty(employeeId));
    checkArgument(!isNullOrEmpty(performanceName));
    checkArgument(!isNullOrEmpty(period));
    checkNotNull(date);
    checkNotNull(stats);

    logger.debug("Started update employee period performance stats: " + companyId + ", " + employeeId + ", " + performanceName + ", " + period + ", " + date + ", " + stats);

    StringBuilder builder = new StringBuilder();
    builder.append("company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName + "/");

    switch (period) {
      case "day":
        builder.append(ofPattern("yyyy/MM/dd").withZone(ZoneId.of("Z")).format(date) + "/_stats");
        break;
      case "month":
        builder.append(ofPattern("yyyy/MM").withZone(ZoneId.of("Z")).format(date) + "/_stats");
        break;
      case "year":
        builder.append(ofPattern("yyyy/").withZone(ZoneId.of("Z")).format(date) + "/_stats");
        break;
      case "all-time":
        builder.append("_stats");
        break;
      default:
        throw new IllegalArgumentException(period);
    }

    String path = builder.toString();

    client.setValue(path, stats);
  }

  @Override
  public void updatePeriodEndDate(String companyId, String employeeId, String performanceName, String period, Instant date, Long periodEndDate) {
    checkArgument(!isNullOrEmpty(companyId));
    checkArgument(!isNullOrEmpty(employeeId));
    checkArgument(!isNullOrEmpty(performanceName));
    checkArgument(!isNullOrEmpty(period));
    checkNotNull(date);
    checkNotNull(periodEndDate);

    logger.debug("Started update employee period performance stats period end date: " + companyId + ", " + employeeId + ", " + performanceName + ", " + period + ", " + periodEndDate);

    StringBuilder builder = new StringBuilder();
    builder.append("company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName + "/");

    switch (period) {
      case "day":
        builder.append(ofPattern("yyyy/MM/dd").withZone(ZoneId.of("Z")).format(date) + "/_stats");
        break;
      case "month":
        builder.append(ofPattern("yyyy/MM").withZone(ZoneId.of("Z")).format(date) + "/_stats");
        break;
      case "year":
        builder.append(ofPattern("yyyy").withZone(ZoneId.of("Z")).format(date) + "/_stats");
        break;
      case "all-time":
        builder.append("_stats");
        break;
      default:
        throw new IllegalArgumentException(period);
    }

    String path = builder.toString();

    client.updateValue(path, of("period_end_date", periodEndDate));
  }

  @Override
  public void deleteAll(String companyId, String employeeId, String performanceName) {
    checkArgument(!isNullOrEmpty(companyId));
    checkArgument(!isNullOrEmpty(employeeId));
    checkArgument(!isNullOrEmpty(performanceName));

    String path = "company_employee_performances/" + companyId + "/" + employeeId + "/" + performanceName;

    Map<String, Object> value = client.getValueAsMap(path, true);

    if (value == null || "null".equals(value)) {
      return;
    }

    for (String key : value.keySet()) {
      String _path = path + "/" + key;

      // all-time
      if ("_stats".equals(key)) {
        client.deleteValue(_path);

        continue;
      }

      Map<String, Object> _value = client.getValueAsMap(_path, true);

      for (String _key : _value.keySet()) {
        String __path = _path + "/" + _key;

        // year
        if ("_stats".equals(_key)) {
          client.deleteValue(__path);

          continue;
        }

        Map<String, Object> __value = client.getValueAsMap(__path, true);

        for (String __key : __value.keySet()) {
          String ___path = __path + "/" + __key;

          // month
          if ("_stats".equals(__key)) {
            client.deleteValue(___path);

            continue;
          }

          Map<String, Object> ___value = client.getValueAsMap(___path, true);

          for (String ___key : ___value.keySet()) {
            String ____path = ___path + "/" + ___key;

            // day
            if ("_stats".equals(___key)) {
              client.deleteValue(____path);

              continue;
            }
          }
        }
      }
    }
  }
}
