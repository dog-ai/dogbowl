/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.stats.presence;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.time.ZonedDateTime.parse;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

public class ComputePresenceStats {

  public Map<String, Object> computeDayStats(Map<String, Map> performance, ZonedDateTime date) {
    if (performance == null || performance.isEmpty()) {
      return null;
    }

    Map<String, Object> stats = new HashMap<>();

    // compute daily total duration stats
    ArrayList<Map<String, Object>> _presences = new ArrayList<>();
    String[] keys = performance.keySet().toArray(new String[performance.keySet().size()]);
    for (int i = 0; i < keys.length; i++) {
      Map<String, Object> cur = (Map<String, Object>) performance.get(keys[i]);

      if ((Boolean) cur.get("is_present")) {

        if (_presences.size() > 0) {
          Map<String, Object> prev = _presences.get(_presences.size() - 1);

          if (!((Boolean) prev.get("is_present"))) {
            _presences.add(cur);
          }

        } else {

          _presences.add(cur);
        }

      } else {

        if (i > 0 && i + 1 < keys.length) {
          Map<String, Object> next = (Map<String, Object>) performance.get(keys[i + 1]);
          ZonedDateTime nextCreatedDate = parse((String) next.get("created_date"), ISO_ZONED_DATE_TIME);
          ZonedDateTime curCreatedDate = parse((String) cur.get("created_date"), ISO_ZONED_DATE_TIME);
          boolean longerThan90min = ChronoUnit.MINUTES.between(curCreatedDate.toInstant(), nextCreatedDate.toInstant()) >= 90;
          if (longerThan90min) {
            _presences.add(cur);
          }
        } else {
          _presences.add(cur);
        }

      }
    }

    int totalDuration = 0;
    for (int i = 0; i < _presences.size(); i++) {
      int diff;
      if ((Boolean) _presences.get(i).get("is_present")) {
        ZonedDateTime next;
        if (i + 1 < _presences.size()) {
          next = parse((String) _presences.get(i + 1).get("created_date"), ISO_ZONED_DATE_TIME);
          diff = (int) ChronoUnit.SECONDS.between(parse((String) _presences.get(i).get("created_date"), ISO_ZONED_DATE_TIME), next);
        } else {
          next = parse((String) _presences.get(i).get("created_date"), ISO_ZONED_DATE_TIME).toLocalDate().atTime(23, 59, 59).atZone(parse((String) _presences.get(i).get("created_date"), ISO_ZONED_DATE_TIME).getZone());
          diff = (int) ChronoUnit.SECONDS.between(parse((String) _presences.get(i).get("created_date"), ISO_ZONED_DATE_TIME), next);
        }
        totalDuration += diff;

      } else if (i == 0) {
        // first presence is not present...assume that the first presence is the beginning of the day
        ZonedDateTime previous = parse((String) _presences.get(i).get("created_date"), ISO_ZONED_DATE_TIME);
        previous = previous.toLocalDate().atStartOfDay(previous.getZone());
        diff = (int) ChronoUnit.SECONDS.between(previous, parse((String) _presences.get(i).get("created_date"), ISO_ZONED_DATE_TIME));

        totalDuration += diff;
      }
    }
    stats.put("total_duration", totalDuration);

    // compute daily start time
    int startTime = 0;
    if (!performance.isEmpty() && (Boolean) performance.get(keys[0]).get("is_present")) {
      ZonedDateTime zonedDateTime = parse((String) performance.get(keys[0]).get("created_date"), ISO_ZONED_DATE_TIME);
      startTime = (int) ChronoUnit.SECONDS.between(zonedDateTime.toLocalDate().atStartOfDay(zonedDateTime.getZone()), zonedDateTime);
    }
    stats.put("start_time", startTime);

    // compute daily end time
    int endTime = 0;
    if (!performance.isEmpty()) {
      ZonedDateTime endDate;
      if ((Boolean) performance.get(keys[keys.length - 1]).get("is_present")) {
        endDate = parse((String) performance.get(keys[keys.length - 1]).get("created_date")).toLocalDate().atTime(23, 59, 59).atZone(parse((String) performance.get(keys[keys.length - 1]).get("created_date")).getZone());
        endTime = (int) ChronoUnit.SECONDS.between(endDate.toLocalDate().atStartOfDay(endDate.getZone()), endDate);
      } else {
        endDate = parse((String) performance.get(keys[keys.length - 1]).get("created_date"));
        endTime = (int) ChronoUnit.SECONDS.between(endDate.toLocalDate().atStartOfDay(endDate.getZone()), endDate);
      }
    }
    stats.put("end_time", endTime);

    ZonedDateTime now = ZonedDateTime.now(date.getZone());
    stats.put("created_date", now.format(ISO_ZONED_DATE_TIME));
    stats.put("updated_date", now.format(ISO_ZONED_DATE_TIME));
    stats.put("period", "day");
    stats.put("period_start_date", date.toLocalDate().atStartOfDay(date.getZone()).format(ISO_ZONED_DATE_TIME));
    stats.put("period_end_date", date.toLocalDate().atTime(23, 59, 59).atZone(date.getZone()).format(ISO_ZONED_DATE_TIME));

    return stats;
  }

  public Map<String, Object> computePeriodStats(final Map<String, Object> dayStats, final Map<String, Object> oldPeriodStats, final String period, final ZonedDateTime date) {
    if (dayStats == null || dayStats.isEmpty()) {
      oldPeriodStats.put("period_end_date", date.toLocalDate().atTime(23, 59, 59).atZone(date.getZone()).format(ISO_ZONED_DATE_TIME));
      return oldPeriodStats;
    }

    if (oldPeriodStats != null && oldPeriodStats.containsKey("period_end_date") && parse((String) dayStats.get("period_start_date"), ISO_ZONED_DATE_TIME).isBefore(parse((String) oldPeriodStats.get("period_end_date"), ISO_ZONED_DATE_TIME))) {
      return oldPeriodStats;
    }

    Map<String, Object> newPeriodStats;
    if (oldPeriodStats != null) {
      newPeriodStats = new TreeMap<>(oldPeriodStats);
    } else {
      newPeriodStats = new TreeMap<>();
    }

    switch (period) {
      case "month":
        // are we starting a new month
        if (oldPeriodStats != null && oldPeriodStats.get("period_start_date") != null && parse((String) oldPeriodStats.get("period_start_date"), ISO_ZONED_DATE_TIME).getMonthValue() != date.getMonthValue()) {
          newPeriodStats = null;
        }
        break;
      case "year":
        // are we starting a new month
        if (oldPeriodStats != null && oldPeriodStats.get("period_start_date") != null && parse((String) oldPeriodStats.get("period_start_date"), ISO_ZONED_DATE_TIME).getYear() != date.getYear()) {
          newPeriodStats = null;
        }
        break;
    }

    ZonedDateTime now = ZonedDateTime.now(date.getZone());

    // no stats
    if (newPeriodStats == null || newPeriodStats.isEmpty()) {
      newPeriodStats = new TreeMap<>();
      newPeriodStats.put("created_date", now.format(ISO_ZONED_DATE_TIME));
      newPeriodStats.put("period_start_date", date.toLocalDate().atStartOfDay(date.getZone()).format(ISO_ZONED_DATE_TIME));
      newPeriodStats.put("updated_date", now.format(ISO_ZONED_DATE_TIME));
      newPeriodStats.put("period_end_date", date.toLocalDate().atTime(23, 59, 59).atZone(date.getZone()).format(ISO_ZONED_DATE_TIME));
      newPeriodStats.put("present_days", 0);
      newPeriodStats.put("average_total_duration", 0.0);
      newPeriodStats.put("average_start_time", 0.0);
      newPeriodStats.put("average_end_time", 0.0);
      newPeriodStats.put("maximum_total_duration", MIN_VALUE);
      newPeriodStats.put("maximum_start_time", MIN_VALUE);
      newPeriodStats.put("maximum_end_time", MIN_VALUE);
      newPeriodStats.put("minimum_total_duration", MAX_VALUE);
      newPeriodStats.put("minimum_start_time", MAX_VALUE);
      newPeriodStats.put("minimum_end_time", MAX_VALUE);

      switch (period) {
        case "month":
          newPeriodStats.put("total_duration_by_day", new TreeMap<>());
          newPeriodStats.put("start_time_by_day", new TreeMap<>());
          newPeriodStats.put("end_time_by_day", new TreeMap<>());
          newPeriodStats.put("total_days", date.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth());
          break;
        case "year":
          newPeriodStats.put("total_days", date.with(TemporalAdjusters.lastDayOfYear()).getDayOfYear());
          break;
      }

    } else {
      newPeriodStats.put("updated_date", now.format(ISO_ZONED_DATE_TIME));
      newPeriodStats.put("period_end_date", date.toLocalDate().atTime(23, 59, 59).atZone(date.getZone()).format(ISO_ZONED_DATE_TIME));
    }

    Map<String, Object> result = computePeriodStatsStartTime(dayStats, newPeriodStats, date, period);
    newPeriodStats.put("minimum_start_time", result.get("minimum_start_time"));
    newPeriodStats.put("maximum_start_time", result.get("maximum_start_time"));
    newPeriodStats.put("average_start_time", result.get("average_start_time"));

    if (period.equals("month")) {
      newPeriodStats.putIfAbsent("start_time_by_day", new TreeMap<Long, Long>());

      ((Map<String, Long>) newPeriodStats.get("start_time_by_day")).put(((Map.Entry<Long, Long>) result.get("start_time_by_day")).getKey().toString(), ((Map.Entry<Long, Long>) result.get("start_time_by_day")).getValue());
    }

    if (period.equals("all-time") && oldPeriodStats != null && oldPeriodStats.containsKey("average_start_time")) {
      newPeriodStats.put("previous_average_start_time", oldPeriodStats.get("average_start_time"));
    }

    result = computePeriodStatsEndTime(dayStats, newPeriodStats, date, period);
    newPeriodStats.put("minimum_end_time", result.get("minimum_end_time"));
    newPeriodStats.put("maximum_end_time", result.get("maximum_end_time"));
    newPeriodStats.put("average_end_time", result.get("average_end_time"));

    if (period.equals("month")) {
      newPeriodStats.putIfAbsent("end_time_by_day", new TreeMap<Long, Long>());

      ((Map<String, Long>) newPeriodStats.get("end_time_by_day")).put(((Map.Entry<Long, Long>) result.get("end_time_by_day")).getKey().toString(), ((Map.Entry<Long, Long>) result.get("end_time_by_day")).getValue());
    }

    if (period.equals("all-time") && oldPeriodStats != null && oldPeriodStats.containsKey("average_end_time")) {
      newPeriodStats.put("previous_average_end_time", oldPeriodStats.get("average_end_time"));
    }

    result = computePeriodStatsTotalDuration(dayStats, newPeriodStats, date, period);
    newPeriodStats.put("minimum_total_duration", result.get("minimum_total_duration"));
    newPeriodStats.put("maximum_total_duration", result.get("maximum_total_duration"));
    newPeriodStats.put("average_total_duration", result.get("average_total_duration"));
    newPeriodStats.put("present_days", result.get("present_days"));

    if (period.equals("month")) {
      newPeriodStats.putIfAbsent("total_duration_by_day", new TreeMap<Long, Long>());

      ((Map<String, Long>) newPeriodStats.get("total_duration_by_day")).put(((Map.Entry<Long, Long>) result.get("total_duration_by_day")).getKey().toString(), ((Map.Entry<Long, Long>) result.get("total_duration_by_day")).getValue());
    }

    if (period.equals("all-time") && oldPeriodStats != null && oldPeriodStats.containsKey("average_total_duration")) {
      newPeriodStats.put("previous_average_total_duration", oldPeriodStats.get("average_total_duration"));
    }

    newPeriodStats.put("period", period);

    return newPeriodStats;
  }

  private Map<String, Object> computePeriodStatsStartTime(Map<String, Object> dayStats, Map<String, Object> periodStats, ZonedDateTime date, String period) {
    Map.Entry<Long, Integer> startTimeByDay = new AbstractMap.SimpleEntry<>(date.toLocalDate().atStartOfDay(date.getZone()).toEpochSecond(), (int) dayStats.get("start_time"));

    int minimumStartTime = Math.min(((int) periodStats.get("minimum_start_time")), (int) dayStats.get("start_time"));

    int maximumStartTime = Math.max(((int) periodStats.get("maximum_start_time")), (int) dayStats.get("start_time"));

    // https://en.wikipedia.org/wiki/Moving_average
    double averageStartTime = (((int) dayStats.get("start_time")) + ((int) periodStats.get("present_days")) * ((double) periodStats.get("average_start_time"))) / (((int) periodStats.get("present_days")) + 1);

    Map<String, Object> periodStatsStartTime = new TreeMap<>();
    periodStatsStartTime.put("minimum_start_time", minimumStartTime);
    periodStatsStartTime.put("maximum_start_time", maximumStartTime);
    periodStatsStartTime.put("average_start_time", averageStartTime);

    if (period.equals("month")) {
      periodStatsStartTime.put("start_time_by_day", startTimeByDay);
    }

    return periodStatsStartTime;
  }

  private Map<String, Object> computePeriodStatsEndTime(Map<String, Object> dayStats, Map<String, Object> periodStats, ZonedDateTime date, String period) {
    Map.Entry<Long, Integer> endTimeByDay = new AbstractMap.SimpleEntry<>(date.toLocalDate().atStartOfDay(date.getZone()).toEpochSecond(), (int) dayStats.get("end_time"));

    int minimumEndTime = Math.min(((int) periodStats.get("minimum_end_time")), (int) dayStats.get("end_time"));

    int maximumEndTime = Math.max(((int) periodStats.get("maximum_end_time")), (int) dayStats.get("end_time"));

    // https://en.wikipedia.org/wiki/Moving_average
    double averageEndTime = (((int) dayStats.get("end_time")) + ((int) periodStats.get("present_days")) * ((double) periodStats.get("average_end_time"))) / (((int) periodStats.get("present_days")) + 1);

    Map<String, Object> periodStatsEndTime = new TreeMap<>();
    periodStatsEndTime.put("minimum_end_time", minimumEndTime);
    periodStatsEndTime.put("maximum_end_time", maximumEndTime);
    periodStatsEndTime.put("average_end_time", averageEndTime);

    if (period.equals("month")) {
      periodStatsEndTime.put("end_time_by_day", endTimeByDay);
    }

    return periodStatsEndTime;
  }

  private Map<String, Object> computePeriodStatsTotalDuration(Map<String, Object> dayStats, Map<String, Object> periodStats, ZonedDateTime date, String period) {
    Map.Entry<Long, Integer> totalDurationByDay = new AbstractMap.SimpleEntry<>(date.toLocalDate().atStartOfDay(date.getZone()).toEpochSecond(), (int) dayStats.get("total_duration"));

    int presentDays = ((int) periodStats.get("present_days")) + 1;

    int minimumTotalDuration = Math.min(((int) periodStats.get("minimum_total_duration")), (int) dayStats.get("total_duration"));

    int maximumTotalDuration = Math.max(((int) periodStats.get("maximum_total_duration")), (int) dayStats.get("total_duration"));

    // https://en.wikipedia.org/wiki/Moving_average
    double averageTotalDuration = (((int) dayStats.get("total_duration")) + ((int) periodStats.get("present_days")) * ((double) periodStats.get("average_total_duration"))) / (((int) periodStats.get("present_days")) + 1);

    Map<String, Object> periodStatsTotalDuration = new TreeMap<>();
    periodStatsTotalDuration.put("minimum_total_duration", minimumTotalDuration);
    periodStatsTotalDuration.put("maximum_total_duration", maximumTotalDuration);
    periodStatsTotalDuration.put("average_total_duration", averageTotalDuration);
    periodStatsTotalDuration.put("present_days", presentDays);

    if (period.equals("month")) {
      periodStatsTotalDuration.put("total_duration_by_day", totalDurationByDay);
    }

    return periodStatsTotalDuration;
  }
}
