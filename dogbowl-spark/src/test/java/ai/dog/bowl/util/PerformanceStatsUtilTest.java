/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ai.dog.bowl.stats.presence.ComputePresenceStats;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

public class PerformanceStatsUtilTest {

  private ComputePresenceStats target;

  @Before
  public void setUp() {
    target = new ComputePresenceStats();
  }

  @Test
  public void shouldComputeEmployeeStatsForDay() {
    ZonedDateTime date = ZonedDateTime.parse("2016-03-08T00:00:00+01:00:00", ISO_ZONED_DATE_TIME);

    Map<String, Map> performance = new TreeMap<>();
    performance.put("my-presence-1", ImmutableMap.of("created_date", "2016-03-08T04:00:00+01:00:00", "is_present", true));
    performance.put("my-presence-2", ImmutableMap.of("created_date", "2016-03-08T08:00:00+01:00:00", "is_present", false));
    performance.put("my-presence-3", ImmutableMap.of("created_date", "2016-03-08T20:00:00+01:00:00", "is_present", true));
    performance.put("my-presence-4", ImmutableMap.of("created_date", "2016-03-08T22:00:00+01:00:00", "is_present", false));

    Map<String, Object> dayStats = target.computeDayStats(performance, date);

    assertThat(dayStats).isNotNull();
    assertThat(dayStats).isNotEmpty();
    assertThat(dayStats.get("total_duration")).isEqualTo(21600);
    assertThat(dayStats.get("start_time")).isEqualTo(14400);
    assertThat(dayStats.get("end_time")).isEqualTo(79200);
    assertThat(dayStats.get("period")).isEqualTo("day");
    assertThat(dayStats.get("period_start_date")).isEqualTo("2016-03-08T00:00:00+01:00");
    assertThat(dayStats.get("period_end_date")).isEqualTo("2016-03-08T23:59:59+01:00");
    assertThat(dayStats.containsKey("created_date")).isTrue();
    assertThat(dayStats.containsKey("updated_date")).isTrue();
    assertThat(dayStats.size()).isEqualTo(8);
  }
  
  @Test
  public void shouldNotComputeEmployeeStatsForDayWhenNoPerformanceAvailable() {
    ZonedDateTime date = ZonedDateTime.parse("2016-03-08T00:00:00+01:00:00", ISO_ZONED_DATE_TIME);

    Map<String, Map> performance = new TreeMap<>();

    Map<String, Object> dayStats = target.computeDayStats(performance, date);

    assertThat(dayStats).isNull();
  }

  @Test
  public void shouldComputeEmployeeStatsForMonth() {
    ZonedDateTime date = ZonedDateTime.parse("2016-03-09T00:00:00+01:00:00", ISO_ZONED_DATE_TIME);

    Map<String, Object> dayStats = new TreeMap<>();
    dayStats.put("created_date", "2016-03-10T03:00:00+01:00");
    dayStats.put("updated_date", "2016-03-10T03:00:00+01:00");
    dayStats.put("period", "day");
    dayStats.put("period_start_date", "2016-03-09T00:00:00+01:00");
    dayStats.put("period_end_date", "2016-03-09T23:59:59+01:00");
    dayStats.put("start_time", 14400);
    dayStats.put("end_time", 79200);
    dayStats.put("total_duration", 21600);

    Map<String, Object> oldMonthStats = new TreeMap<>();
    oldMonthStats.put("created_date", "2016-03-10T03:00:00+01:00");
    oldMonthStats.put("updated_date", "2016-03-10T03:00:00+01:00");
    oldMonthStats.put("period", "month");
    oldMonthStats.put("period_start_date", "2016-03-08T00:00:00+01:00");
    oldMonthStats.put("period_end_date", "2016-03-08T23:59:59+01:00");
    oldMonthStats.put("total_days", 31);
    oldMonthStats.put("present_days", 1);
    oldMonthStats.put("average_start_time", 14400.0);
    oldMonthStats.put("average_end_time", 79200.0);
    oldMonthStats.put("average_total_duration", 21600.0);
    oldMonthStats.put("maximum_start_time", 14400);
    oldMonthStats.put("maximum_end_time", 79200);
    oldMonthStats.put("maximum_total_duration", 21600);
    oldMonthStats.put("minimum_start_time", 14400);
    oldMonthStats.put("minimum_end_time", 79200);
    oldMonthStats.put("minimum_total_duration", 21600);
    oldMonthStats.put("start_time_by_day", new HashMap<String, Integer>() {{put("1457391600", 14400);}});
    oldMonthStats.put("end_time_by_day", new HashMap<String, Integer>() {{put("1457391600", 79200);}});
    oldMonthStats.put("total_duration_by_day", new HashMap<String, Integer>() {{put("1457391600", 21600);}});

    String period = "month";
    Map<String, Object> newMonthStats = target.computePeriodStats(dayStats, oldMonthStats, period, date);

    assertThat(newMonthStats).isNotNull();
    assertThat(newMonthStats).isNotEmpty();
    assertThat(newMonthStats.get("period")).isEqualTo(period);
    assertThat(newMonthStats.get("total_days")).isEqualTo(31);
    assertThat(newMonthStats.get("present_days")).isEqualTo(2);
    assertThat(newMonthStats.get("average_start_time")).isEqualTo(14400.0);
    assertThat(newMonthStats.get("average_end_time")).isEqualTo(79200.0);
    assertThat(newMonthStats.get("average_total_duration")).isEqualTo(21600.0);
    assertThat(newMonthStats.get("minimum_start_time")).isEqualTo(14400);
    assertThat(newMonthStats.get("minimum_end_time")).isEqualTo(79200);
    assertThat(newMonthStats.get("minimum_total_duration")).isEqualTo(21600);
    assertThat(newMonthStats.get("maximum_start_time")).isEqualTo(14400);
    assertThat(newMonthStats.get("maximum_end_time")).isEqualTo(79200);
    assertThat(newMonthStats.get("maximum_total_duration")).isEqualTo(21600);
    assertThat(newMonthStats.get("period_start_date")).isEqualTo("2016-03-08T00:00:00+01:00");
    assertThat(newMonthStats.get("period_end_date")).isEqualTo("2016-03-09T23:59:59+01:00");
    assertThat(newMonthStats.containsKey("start_time_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("end_time_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("total_duration_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("created_date")).isTrue();
    assertThat(newMonthStats.containsKey("updated_date")).isTrue();
    assertThat(newMonthStats.size()).isEqualTo(19);
  }

  @Test
  public void shouldNotComputeEmployeeStatsForMonthWhenNoDayStatsAvailable() {
    ZonedDateTime date = ZonedDateTime.parse("2016-03-09T00:00:00+01:00:00", ISO_ZONED_DATE_TIME);

    Map<String, Object> dayStats = null;

    Map<String, Object> oldMonthStats = new TreeMap<>();
    oldMonthStats.put("created_date", "2016-03-09T03:00:00+01:00");
    oldMonthStats.put("updated_date", "2016-03-09T03:00:00+01:00");
    oldMonthStats.put("period", "month");
    oldMonthStats.put("period_start_date", "2016-03-08T00:00:00+01:00");
    oldMonthStats.put("period_end_date", "2016-03-08T23:59:59+01:00");
    oldMonthStats.put("total_days", 31);
    oldMonthStats.put("present_days", 1);
    oldMonthStats.put("average_start_time", 14400.0);
    oldMonthStats.put("average_end_time", 79200.0);
    oldMonthStats.put("average_total_duration", 21600.0);
    oldMonthStats.put("maximum_start_time", 14400);
    oldMonthStats.put("maximum_end_time", 79200);
    oldMonthStats.put("maximum_total_duration", 21600);
    oldMonthStats.put("minimum_start_time", 14400);
    oldMonthStats.put("minimum_end_time", 79200);
    oldMonthStats.put("minimum_total_duration", 21600);
    oldMonthStats.put("start_time_by_day", new TreeMap<String, Integer>() {{put("1457391600", 14400);}});
    oldMonthStats.put("end_time_by_day", new TreeMap<String, Integer>() {{put("1457391600", 79200);}});
    oldMonthStats.put("total_duration_by_day", new TreeMap<String, Integer>() {{put("1457391600", 21600);}});

    Map<String, Object> newMonthStats = target.computePeriodStats(dayStats, oldMonthStats, "month", date);

    assertThat(newMonthStats).isNotNull();
    assertThat(newMonthStats).isNotEmpty();
    assertThat(newMonthStats.get("period")).isEqualTo(oldMonthStats.get("period"));
    assertThat(newMonthStats.get("total_days")).isEqualTo(oldMonthStats.get("total_days"));
    assertThat(newMonthStats.get("present_days")).isEqualTo(oldMonthStats.get("present_days"));
    assertThat(newMonthStats.get("average_start_time")).isEqualTo(oldMonthStats.get("average_start_time"));
    assertThat(newMonthStats.get("average_end_time")).isEqualTo(oldMonthStats.get("average_end_time"));
    assertThat(newMonthStats.get("average_total_duration")).isEqualTo(oldMonthStats.get("average_total_duration"));
    assertThat(newMonthStats.get("minimum_start_time")).isEqualTo(oldMonthStats.get("minimum_start_time"));
    assertThat(newMonthStats.get("minimum_end_time")).isEqualTo(oldMonthStats.get("minimum_end_time"));
    assertThat(newMonthStats.get("minimum_total_duration")).isEqualTo(oldMonthStats.get("minimum_total_duration"));
    assertThat(newMonthStats.get("maximum_start_time")).isEqualTo(oldMonthStats.get("maximum_start_time"));
    assertThat(newMonthStats.get("maximum_end_time")).isEqualTo(oldMonthStats.get("maximum_end_time"));
    assertThat(newMonthStats.get("maximum_total_duration")).isEqualTo(oldMonthStats.get("maximum_total_duration"));
    assertThat(newMonthStats.get("period_start_date")).isEqualTo(oldMonthStats.get("period_start_date"));
    assertThat(newMonthStats.get("period_end_date")).isEqualTo("2016-03-09T23:59:59+01:00");
    assertThat(newMonthStats.containsKey("start_time_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("end_time_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("total_duration_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("created_date")).isTrue();
    assertThat(newMonthStats.containsKey("updated_date")).isTrue();
    assertThat(newMonthStats.size()).isEqualTo(oldMonthStats.size());
  }

  @Test
  public void shouldComputeEmployeeStatsForMonthWhenNoOldMonthStatsAvailable() {
    ZonedDateTime date = ZonedDateTime.parse("2016-03-09T00:00:00+01:00:00", ISO_ZONED_DATE_TIME);

    Map<String, Object> dayStats = new TreeMap<>();
    dayStats.put("created_date", "2016-03-10T03:00:00+01:00");
    dayStats.put("updated_date", "2016-03-10T03:00:00+01:00");
    dayStats.put("period", "day");
    dayStats.put("period_start_date", "2016-03-09T00:00:00+01:00");
    dayStats.put("period_end_date", "2016-03-09T23:59:59+01:00");
    dayStats.put("start_time", 14400);
    dayStats.put("end_time", 79200);
    dayStats.put("total_duration", 21600);

    Map<String, Object> oldMonthStats = null;

    String period = "month";

    Map<String, Object> newMonthStats = target.computePeriodStats(dayStats, oldMonthStats, period, date);

    assertThat(newMonthStats).isNotNull();
    assertThat(newMonthStats).isNotEmpty();
    assertThat(newMonthStats.get("period")).isEqualTo(period);
    assertThat(newMonthStats.get("total_days")).isEqualTo(31);
    assertThat(newMonthStats.get("present_days")).isEqualTo(1);
    assertThat(newMonthStats.get("average_start_time")).isEqualTo(14400.0);
    assertThat(newMonthStats.get("average_end_time")).isEqualTo(79200.0);
    assertThat(newMonthStats.get("average_total_duration")).isEqualTo(21600.0);
    assertThat(newMonthStats.get("minimum_start_time")).isEqualTo(14400);
    assertThat(newMonthStats.get("minimum_end_time")).isEqualTo(79200);
    assertThat(newMonthStats.get("minimum_total_duration")).isEqualTo(21600);
    assertThat(newMonthStats.get("maximum_start_time")).isEqualTo(14400);
    assertThat(newMonthStats.get("maximum_end_time")).isEqualTo(79200);
    assertThat(newMonthStats.get("maximum_total_duration")).isEqualTo(21600);
    assertThat(newMonthStats.get("period_start_date")).isEqualTo("2016-03-09T00:00:00+01:00");
    assertThat(newMonthStats.get("period_end_date")).isEqualTo("2016-03-09T23:59:59+01:00");
    assertThat(newMonthStats.containsKey("start_time_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("end_time_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("total_duration_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("created_date")).isTrue();
    assertThat(newMonthStats.containsKey("updated_date")).isTrue();
    assertThat(newMonthStats.size()).isEqualTo(19);
  }

  @Test
  public void shouldComputeEmployeeStatsForYear() {
    ZonedDateTime date = ZonedDateTime.parse("2016-03-09T00:00:00+01:00:00", ISO_ZONED_DATE_TIME);

    Map<String, Object> dayStats = new TreeMap<>();
    dayStats.put("created_date", "2016-03-10T03:00:00+01:00");
    dayStats.put("updated_date", "2016-03-10T03:00:00+01:00");
    dayStats.put("period", "day");
    dayStats.put("period_start_date", "2016-03-09T00:00:00+01:00");
    dayStats.put("period_end_date", "2016-03-09T23:59:59+01:00");
    dayStats.put("start_time", 14400);
    dayStats.put("end_time", 79200);
    dayStats.put("total_duration", 21600);

    Map<String, Object> oldYearStats = new TreeMap<>();
    oldYearStats.put("created_date", "2016-03-10T03:00:00+01:00");
    oldYearStats.put("updated_date", "2016-03-10T03:00:00+01:00");
    oldYearStats.put("period", "year");
    oldYearStats.put("period_start_date", "2016-03-08T00:00:00+01:00");
    oldYearStats.put("period_end_date", "2016-03-08T23:59:59+01:00");
    oldYearStats.put("total_days", 365);
    oldYearStats.put("present_days", 1);
    oldYearStats.put("average_start_time", 14400.0);
    oldYearStats.put("average_end_time", 79200.0);
    oldYearStats.put("average_total_duration", 21600.0);
    oldYearStats.put("maximum_start_time", 14400);
    oldYearStats.put("maximum_end_time", 79200);
    oldYearStats.put("maximum_total_duration", 21600);
    oldYearStats.put("minimum_start_time", 14400);
    oldYearStats.put("minimum_end_time", 79200);
    oldYearStats.put("minimum_total_duration", 21600);

    String period = "year";
    Map<String, Object> newYearStats = target.computePeriodStats(dayStats, oldYearStats, period, date);

    assertThat(newYearStats).isNotNull();
    assertThat(newYearStats).isNotEmpty();
    assertThat(newYearStats.get("period")).isEqualTo(period);
    assertThat(newYearStats.get("total_days")).isEqualTo(365);
    assertThat(newYearStats.get("present_days")).isEqualTo(2);
    assertThat(newYearStats.get("average_start_time")).isEqualTo(14400.0);
    assertThat(newYearStats.get("average_end_time")).isEqualTo(79200.0);
    assertThat(newYearStats.get("average_total_duration")).isEqualTo(21600.0);
    assertThat(newYearStats.get("minimum_start_time")).isEqualTo(14400);
    assertThat(newYearStats.get("minimum_end_time")).isEqualTo(79200);
    assertThat(newYearStats.get("minimum_total_duration")).isEqualTo(21600);
    assertThat(newYearStats.get("maximum_start_time")).isEqualTo(14400);
    assertThat(newYearStats.get("maximum_end_time")).isEqualTo(79200);
    assertThat(newYearStats.get("maximum_total_duration")).isEqualTo(21600);
    assertThat(newYearStats.get("period_start_date")).isEqualTo("2016-03-08T00:00:00+01:00");
    assertThat(newYearStats.get("period_end_date")).isEqualTo("2016-03-09T23:59:59+01:00");
    assertThat(newYearStats.containsKey("created_date")).isTrue();
    assertThat(newYearStats.containsKey("updated_date")).isTrue();
    assertThat(newYearStats.size()).isEqualTo(16);
  }

  @Test
  public void shouldComputeEmployeeStatsForAllTime() {
    ZonedDateTime date = ZonedDateTime.parse("2016-03-09T00:00:00+01:00:00", ISO_ZONED_DATE_TIME);

    Map<String, Object> dayStats = new TreeMap<>();
    dayStats.put("created_date", "2016-03-10T03:00:00+01:00");
    dayStats.put("updated_date", "2016-03-10T03:00:00+01:00");
    dayStats.put("period", "day");
    dayStats.put("period_start_date", "2016-03-09T00:00:00+01:00");
    dayStats.put("period_end_date", "2016-03-09T23:59:59+01:00");
    dayStats.put("start_time", 14400);
    dayStats.put("end_time", 79200);
    dayStats.put("total_duration", 21600);

    Map<String, Object> oldAllTimeStats = new TreeMap<>();
    oldAllTimeStats.put("created_date", "2016-03-10T03:00:00+01:00");
    oldAllTimeStats.put("updated_date", "2016-03-10T03:00:00+01:00");
    oldAllTimeStats.put("period", "all-time");
    oldAllTimeStats.put("period_start_date", "2016-03-08T00:00:00+01:00");
    oldAllTimeStats.put("period_end_date", "2016-03-08T23:59:59+01:00");
    oldAllTimeStats.put("total_days", 365);
    oldAllTimeStats.put("present_days", 1);
    oldAllTimeStats.put("average_start_time", 14400.0);
    oldAllTimeStats.put("average_end_time", 79200.0);
    oldAllTimeStats.put("average_total_duration", 21600.0);
    oldAllTimeStats.put("maximum_start_time", 14400);
    oldAllTimeStats.put("maximum_end_time", 79200);
    oldAllTimeStats.put("maximum_total_duration", 21600);
    oldAllTimeStats.put("minimum_start_time", 14400);
    oldAllTimeStats.put("minimum_end_time", 79200);
    oldAllTimeStats.put("minimum_total_duration", 21600);
    oldAllTimeStats.put("previous_average_start_time", 14400);
    oldAllTimeStats.put("previous_average_end_time", 79200);
    oldAllTimeStats.put("previous_average_total_duration", 21600);

    String period = "all-time";
    Map<String, Object> newAllTimeStats = target.computePeriodStats(dayStats, oldAllTimeStats, period, date);

    assertThat(newAllTimeStats).isNotNull();
    assertThat(newAllTimeStats).isNotEmpty();
    assertThat(newAllTimeStats.get("period")).isEqualTo(period);
    assertThat(newAllTimeStats.get("total_days")).isEqualTo(365);
    assertThat(newAllTimeStats.get("present_days")).isEqualTo(2);
    assertThat(newAllTimeStats.get("average_start_time")).isEqualTo(14400.0);
    assertThat(newAllTimeStats.get("average_end_time")).isEqualTo(79200.0);
    assertThat(newAllTimeStats.get("average_total_duration")).isEqualTo(21600.0);
    assertThat(newAllTimeStats.get("minimum_start_time")).isEqualTo(14400);
    assertThat(newAllTimeStats.get("minimum_end_time")).isEqualTo(79200);
    assertThat(newAllTimeStats.get("minimum_total_duration")).isEqualTo(21600);
    assertThat(newAllTimeStats.get("maximum_start_time")).isEqualTo(14400);
    assertThat(newAllTimeStats.get("maximum_end_time")).isEqualTo(79200);
    assertThat(newAllTimeStats.get("maximum_total_duration")).isEqualTo(21600);
    assertThat(newAllTimeStats.get("previous_average_start_time")).isEqualTo(14400.0);
    assertThat(newAllTimeStats.get("previous_average_end_time")).isEqualTo(79200.0);
    assertThat(newAllTimeStats.get("previous_average_total_duration")).isEqualTo(21600.0);
    assertThat(newAllTimeStats.get("period_start_date")).isEqualTo("2016-03-08T00:00:00+01:00");
    assertThat(newAllTimeStats.get("period_end_date")).isEqualTo("2016-03-09T23:59:59+01:00");
    assertThat(newAllTimeStats.containsKey("created_date")).isTrue();
    assertThat(newAllTimeStats.containsKey("updated_date")).isTrue();
    assertThat(newAllTimeStats.size()).isEqualTo(19);
  }
}
