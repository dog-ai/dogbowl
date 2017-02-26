/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.stats.presence;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.AbstractMap;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class ComputePeriodStats {
  public Map<String, Object> compute(final Map<String, Object> dayStats, final Map<String, Object> oldPeriodStats, final String period, final Instant date) {
    if (dayStats == null || dayStats.isEmpty()) {
      oldPeriodStats.put("period_end_date", date.atZone(ZoneId.of("Z")).toLocalDate().atTime(23, 59, 59).atZone(ZoneId.of("Z")).toInstant().toEpochMilli() / 1000);
      return oldPeriodStats;
    }

    // TODO: temp
    Instant oldPeriodEndDate = null;
    if (oldPeriodStats != null && oldPeriodStats.containsKey("period_end_date") && oldPeriodStats.get("period_end_date") instanceof String) {
      oldPeriodEndDate = ZonedDateTime.parse((String) oldPeriodStats.get("period_end_date")).toInstant();
    } else if (oldPeriodStats != null && oldPeriodStats.containsKey("period_end_date")) {
      oldPeriodEndDate = Instant.ofEpochSecond((Long) oldPeriodStats.get("period_end_date"));
    }

    // TODO: temp
    Instant oldPeriodStartDate = null;
    if (oldPeriodStats != null && oldPeriodStats.containsKey("period_start_date") && oldPeriodStats.get("period_start_date") instanceof String) {
      oldPeriodStartDate = ZonedDateTime.parse((String) oldPeriodStats.get("period_start_date")).toInstant();
    } else if (oldPeriodStats != null && oldPeriodStats.containsKey("period_start_date")) {
      oldPeriodStartDate = Instant.ofEpochSecond((Long) oldPeriodStats.get("period_start_date"));
    }

    if (oldPeriodStats != null && oldPeriodStats.containsKey("period_end_date") && Instant.ofEpochSecond((Long) dayStats.get("period_start_date")).isBefore(oldPeriodEndDate)) {
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
        if (oldPeriodStats != null && oldPeriodStats.containsKey("period_start_date") && oldPeriodStartDate.atZone(ZoneId.of("Z")).getMonthValue() != date.atZone(ZoneId.of("Z")).getMonthValue()) {
          newPeriodStats = null;
        }
        break;
      case "year":
        // are we starting a new year
        if (oldPeriodStats != null && oldPeriodStats.containsKey("period_start_date") && oldPeriodStartDate.atZone(ZoneId.of("Z")).getYear() != date.atZone(ZoneId.of("Z")).getYear()) {
          newPeriodStats = null;
        }
        break;
    }

    Instant now = Instant.now();

    // no stats
    if (newPeriodStats == null || newPeriodStats.isEmpty()) {
      newPeriodStats = new TreeMap<>();
      newPeriodStats.put("created_date", now.toEpochMilli() / 1000);
      newPeriodStats.put("period_start_date", date.atZone(ZoneId.of("Z")).toLocalDate().atStartOfDay(ZoneId.of("Z")).toInstant().toEpochMilli() / 1000);
      newPeriodStats.put("updated_date", now.toEpochMilli() / 1000);
      newPeriodStats.put("period_end_date", date.atZone(ZoneId.of("Z")).toLocalDate().atTime(23, 59, 59).atZone(ZoneId.of("Z")).toInstant().toEpochMilli() / 1000);
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
          newPeriodStats.put("total_days", date.atZone(ZoneId.of("Z")).with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth());
          break;
        case "year":
          newPeriodStats.put("total_days", date.atZone(ZoneId.of("Z")).with(TemporalAdjusters.lastDayOfYear()).getDayOfYear());
          break;
      }

    } else {
      newPeriodStats.put("updated_date", now.toEpochMilli() / 1000);
      newPeriodStats.put("period_end_date", date.atZone(ZoneId.of("Z")).toLocalDate().atTime(23, 59, 59).atZone(ZoneId.of("Z")).toInstant().toEpochMilli() / 1000);
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

  private Map<String, Object> computePeriodStatsStartTime(Map<String, Object> dayStats, Map<String, Object> periodStats, Instant date, String period) {
    int dayStartTime = (int) dayStats.get("start_time");

    int minimumStartTime = min(((int) periodStats.get("minimum_start_time")), dayStartTime);
    int maximumStartTime = max(((int) periodStats.get("maximum_start_time")), dayStartTime);

    // https://en.wikipedia.org/wiki/Moving_average
    double averageStartTime = (dayStartTime + ((int) periodStats.get("present_days")) * ((double) periodStats.get("average_start_time"))) / (((int) periodStats.get("present_days")) + 1);

    Map<String, Object> periodStatsStartTime = new TreeMap<>();
    periodStatsStartTime.put("minimum_start_time", minimumStartTime);
    periodStatsStartTime.put("maximum_start_time", maximumStartTime);
    periodStatsStartTime.put("average_start_time", averageStartTime);

    if (period.equals("month")) {
      Long dateEpochSecond = date.atZone(ZoneId.of("Z")).toLocalDate().atStartOfDay(ZoneId.of("Z")).toInstant().toEpochMilli() / 1000;
      Map.Entry<Long, Integer> startTimeByDay = new AbstractMap.SimpleEntry<>(dateEpochSecond, dayStartTime);

      periodStatsStartTime.put("start_time_by_day", startTimeByDay);
    }

    return periodStatsStartTime;
  }

  private Map<String, Object> computePeriodStatsEndTime(Map<String, Object> dayStats, Map<String, Object> periodStats, Instant date, String period) {
    int dayEndTime = (int) dayStats.get("end_time");

    int minimumEndTime = min(((int) periodStats.get("minimum_end_time")), (int) dayStats.get("end_time"));
    int maximumEndTime = max(((int) periodStats.get("maximum_end_time")), (int) dayStats.get("end_time"));

    // https://en.wikipedia.org/wiki/Moving_average
    double averageEndTime = (dayEndTime + ((int) periodStats.get("present_days")) * ((double) periodStats.get("average_end_time"))) / (((int) periodStats.get("present_days")) + 1);

    Map<String, Object> periodStatsEndTime = new TreeMap<>();
    periodStatsEndTime.put("minimum_end_time", minimumEndTime);
    periodStatsEndTime.put("maximum_end_time", maximumEndTime);
    periodStatsEndTime.put("average_end_time", averageEndTime);

    if (period.equals("month")) {
      Long dateEpochSecond = date.atZone(ZoneId.of("Z")).toLocalDate().atStartOfDay(ZoneId.of("Z")).toInstant().toEpochMilli() / 1000;
      Map.Entry<Long, Integer> endTimeByDay = new AbstractMap.SimpleEntry<>(dateEpochSecond, dayEndTime);

      periodStatsEndTime.put("end_time_by_day", endTimeByDay);
    }

    return periodStatsEndTime;
  }

  private Map<String, Object> computePeriodStatsTotalDuration(Map<String, Object> dayStats, Map<String, Object> periodStats, Instant date, String period) {
    int dayTotalDuration = (int) dayStats.get("total_duration");
    int presentDays = ((int) periodStats.get("present_days")) + 1;

    int minimumTotalDuration = min(((int) periodStats.get("minimum_total_duration")), dayTotalDuration);
    int maximumTotalDuration = max(((int) periodStats.get("maximum_total_duration")), dayTotalDuration);

    // https://en.wikipedia.org/wiki/Moving_average
    double averageTotalDuration = (dayTotalDuration + ((int) periodStats.get("present_days")) * ((double) periodStats.get("average_total_duration"))) / (((int) periodStats.get("present_days")) + 1);

    Map<String, Object> periodStatsTotalDuration = new TreeMap<>();
    periodStatsTotalDuration.put("minimum_total_duration", minimumTotalDuration);
    periodStatsTotalDuration.put("maximum_total_duration", maximumTotalDuration);
    periodStatsTotalDuration.put("average_total_duration", averageTotalDuration);
    periodStatsTotalDuration.put("present_days", presentDays);

    if (period.equals("month")) {
      Long dateEpochSecond = date.atZone(ZoneId.of("Z")).toLocalDate().atStartOfDay(ZoneId.of("Z")).toInstant().toEpochMilli() / 1000;
      Map.Entry<Long, Integer> totalDurationByDay = new AbstractMap.SimpleEntry<>(dateEpochSecond, dayTotalDuration);

      periodStatsTotalDuration.put("total_duration_by_day", totalDurationByDay);
    }

    return periodStatsTotalDuration;
  }
}
