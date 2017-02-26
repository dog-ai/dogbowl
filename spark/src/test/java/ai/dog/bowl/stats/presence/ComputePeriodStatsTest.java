/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.stats.presence;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ComputePeriodStatsTest {

  private ComputePeriodStats target;

  @Before
  public void setUp() {
    target = new ComputePeriodStats();
  }

  @Test
  public void shouldComputeEmployeeStatsForMonth() {
    Instant date = Instant.parse("2016-03-08T00:00:00Z");

    Map<String, Object> dayStats = new TreeMap<>();
    dayStats.put("created_date", 1457481601L); // 2016-03-09T00:00:01Z
    dayStats.put("created_date", 1457481601L); // 2016-03-09T00:00:01Z
    dayStats.put("period", "day");
    dayStats.put("period_start_date", 1457395200L); // 2016-03-08T00:00:00Z
    dayStats.put("period_end_date", 1457481599L); // 2016-03-08T23:59:59Z
    dayStats.put("start_time", 14400);
    dayStats.put("end_time", 79200);
    dayStats.put("total_duration", 21600);

    Map<String, Object> oldMonthStats = new TreeMap<>();
    oldMonthStats.put("period", "month");
    oldMonthStats.put("created_date", 1457395201L); // 2016-03-08T00:00:01Z
    oldMonthStats.put("updated_date", 1457395201L); // 2016-03-08T00:00:01Z
    oldMonthStats.put("period_start_date", 1457308800L); // 2016-03-07T00:00:00Z
    oldMonthStats.put("period_end_date", 1457395199L); // 2016-03-07T23:59:59Z
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
    oldMonthStats.put("start_time_by_day", new HashMap<String, Integer>() {{
      put("1457395200", 14400);
    }}); // 2016-03-08T00:00:00Z
    oldMonthStats.put("end_time_by_day", new HashMap<String, Integer>() {{
      put("1457395200", 79200);
    }}); // 2016-03-08T00:00:00Z
    oldMonthStats.put("total_duration_by_day", new HashMap<String, Integer>() {{
      put("1457395200", 21600);
    }}); // 2016-03-08T00:00:00Z

    String period = "month";
    Map<String, Object> newMonthStats = target.compute(dayStats, oldMonthStats, period, date);

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
    assertThat(newMonthStats.get("period_start_date")).isEqualTo(1457308800L); // 2016-03-07T00:00:00Z
    assertThat(newMonthStats.get("period_end_date")).isEqualTo(1457481599L); // 2016-03-08T23:59:59Z
    assertThat(newMonthStats.containsKey("start_time_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("end_time_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("total_duration_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("created_date")).isTrue();
    assertThat(newMonthStats.containsKey("updated_date")).isTrue();
    assertThat(newMonthStats.size()).isEqualTo(19);
  }

  @Test
  public void shouldNotComputeEmployeeStatsForMonthWhenNoDayStatsAvailable() {
    Instant date = Instant.parse("2016-03-09T00:00:00Z");

    Map<String, Object> dayStats = null;

    Map<String, Object> oldMonthStats = new TreeMap<>();
    oldMonthStats.put("created_date", 1457492400L); // 2016-03-09T03:00:00Z
    oldMonthStats.put("updated_date", 1457492400L); // 2016-03-09T03:00:00Z
    oldMonthStats.put("period", "month");
    oldMonthStats.put("period_start_date", 1457395200L); // 2016-03-08T00:00:00Z
    oldMonthStats.put("period_end_date", 1457481599L); // 2016-03-08T23:59:59Z
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
    oldMonthStats.put("start_time_by_day", new TreeMap<String, Integer>() {{
      put("1457395200", 14400);
    }}); // 2016-03-08T00:00:00Z
    oldMonthStats.put("end_time_by_day", new TreeMap<String, Integer>() {{
      put("1457395200", 79200);
    }}); // 2016-03-08T00:00:00Z
    oldMonthStats.put("total_duration_by_day", new TreeMap<String, Integer>() {{
      put("1457395200", 21600);
    }}); // 2016-03-08T00:00:00Z

    Map<String, Object> newMonthStats = target.compute(dayStats, oldMonthStats, "month", date);

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
    assertThat(newMonthStats.get("period_end_date")).isEqualTo(1457567999L); // 2016-03-09T23:59:59Z
    assertThat(newMonthStats.containsKey("start_time_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("end_time_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("total_duration_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("created_date")).isTrue();
    assertThat(newMonthStats.containsKey("updated_date")).isTrue();
    assertThat(newMonthStats.size()).isEqualTo(oldMonthStats.size());
  }

  @Test
  public void shouldComputeEmployeeStatsForMonthWhenNoOldMonthStatsAvailable() {
    Instant date = Instant.parse("2016-03-09T00:00:00Z");

    Map<String, Object> dayStats = new TreeMap<>();
    dayStats.put("created_date", 1457578800L); // 2016-03-10T03:00:00Z
    dayStats.put("updated_date", 1457578800L); // 2016-03-10T03:00:00Z
    dayStats.put("period", "day");
    dayStats.put("period_start_date", 1457481600L); // 2016-03-09T00:00:00Z
    dayStats.put("period_end_date", 1457567999L); // 2016-03-09T23:59:59Z
    dayStats.put("start_time", 14400);
    dayStats.put("end_time", 79200);
    dayStats.put("total_duration", 21600);

    Map<String, Object> oldMonthStats = null;

    String period = "month";

    Map<String, Object> newMonthStats = target.compute(dayStats, oldMonthStats, period, date);

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
    assertThat(newMonthStats.get("period_start_date")).isEqualTo(1457481600L); // 2016-03-09T00:00:00Z
    assertThat(newMonthStats.get("period_end_date")).isEqualTo(1457567999L); // 2016-03-09T23:59:59Z
    assertThat(newMonthStats.containsKey("start_time_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("end_time_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("total_duration_by_day")).isTrue();
    assertThat(newMonthStats.containsKey("created_date")).isTrue();
    assertThat(newMonthStats.containsKey("updated_date")).isTrue();
    assertThat(newMonthStats.size()).isEqualTo(19);
  }

  @Test
  public void shouldComputeEmployeeStatsForYear() {
    Instant date = Instant.parse("2016-03-09T00:00:00Z");

    Map<String, Object> dayStats = new TreeMap<>();
    dayStats.put("created_date", 1457578800L); // 2016-03-10T03:00:00Z
    dayStats.put("updated_date", 1457578800L); // 2016-03-10T03:00:00Z
    dayStats.put("period", "day");
    dayStats.put("period_start_date", 1457481600L); // 2016-03-09T00:00:00Z
    dayStats.put("period_end_date", 1457567999L); // 2016-03-09T23:59:59Z
    dayStats.put("start_time", 14400);
    dayStats.put("end_time", 79200);
    dayStats.put("total_duration", 21600);

    Map<String, Object> oldYearStats = new TreeMap<>();
    oldYearStats.put("created_date", 1457578800L); // 2016-03-10T03:00:00Z
    oldYearStats.put("updated_date", 1457578800L); // 2016-03-10T03:00:00Z
    oldYearStats.put("period", "year");
    oldYearStats.put("period_start_date", 1457395200L); // 2016-03-08T00:00:00Z
    oldYearStats.put("period_end_date", 1457481599L); // 2016-03-08T23:59:59Z
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
    Map<String, Object> newYearStats = target.compute(dayStats, oldYearStats, period, date);

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
    assertThat(newYearStats.get("period_start_date")).isEqualTo(1457395200L); // 2016-03-08T00:00:00Z
    assertThat(newYearStats.get("period_end_date")).isEqualTo(1457567999L); // 2016-03-09T23:59:59Z
    assertThat(newYearStats.containsKey("created_date")).isTrue();
    assertThat(newYearStats.containsKey("updated_date")).isTrue();
    assertThat(newYearStats.size()).isEqualTo(16);
  }

  @Test
  public void shouldComputeEmployeeStatsForAllTime() {
    Instant date = Instant.parse("2016-03-09T00:00:00Z");

    Map<String, Object> dayStats = new TreeMap<>();
    dayStats.put("created_date", 1457578800L); // 2016-03-10T03:00:00Z
    dayStats.put("updated_date", 1457578800L); // 2016-03-10T03:00:00Z
    dayStats.put("period", "day");
    dayStats.put("period_start_date", 1457481600L); // 2016-03-09T00:00:00Z
    dayStats.put("period_end_date", 1457567999L); // 2016-03-09T23:59:59Z
    dayStats.put("start_time", 14400);
    dayStats.put("end_time", 79200);
    dayStats.put("total_duration", 21600);

    Map<String, Object> oldAllTimeStats = new TreeMap<>();
    oldAllTimeStats.put("created_date", 1457578800L); // 2016-03-10T03:00:00Z
    oldAllTimeStats.put("updated_date", 1457578800L); // 2016-03-10T03:00:00Z
    oldAllTimeStats.put("period", "all-time");
    oldAllTimeStats.put("period_start_date", 1457395200L); // 2016-03-08T00:00:00Z
    oldAllTimeStats.put("period_end_date", 1457481599L); // 2016-03-08T23:59:59Z
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
    Map<String, Object> newAllTimeStats = target.compute(dayStats, oldAllTimeStats, period, date);

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
    assertThat(newAllTimeStats.get("period_start_date")).isEqualTo(1457395200L); // 2016-03-08T00:00:00Z
    assertThat(newAllTimeStats.get("period_end_date")).isEqualTo(1457567999L); // 2016-03-09T23:59:59Z
    assertThat(newAllTimeStats.containsKey("created_date")).isTrue();
    assertThat(newAllTimeStats.containsKey("updated_date")).isTrue();
    assertThat(newAllTimeStats.size()).isEqualTo(19);
  }
}
