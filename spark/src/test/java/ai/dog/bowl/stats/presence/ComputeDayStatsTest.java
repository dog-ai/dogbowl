/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.stats.presence;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ComputeDayStatsTest {

  private ComputeDayStats target;

  @Before
  public void setUp() {
    target = new ComputeDayStats();
  }

  @Test
  public void shouldComputeEmployeeStatsForDay() {
    Instant date = Instant.ofEpochSecond(1457395200);

    Map<String, Map> performance = new TreeMap<>();
    performance.put("my-presence-1", ImmutableMap.of("created_date", 1457409600, "is_present", true));
    performance.put("my-presence-2", ImmutableMap.of("created_date", 1457424000, "is_present", false));
    performance.put("my-presence-3", ImmutableMap.of("created_date", 1457467200, "is_present", true));
    performance.put("my-presence-4", ImmutableMap.of("created_date", 1457474400, "is_present", false));

    Map<String, Object> dayStats = target.compute(performance, date);

    assertThat(dayStats).isNotNull();
    assertThat(dayStats).isNotEmpty();
    assertThat(dayStats.get("total_duration")).isEqualTo(21600);
    assertThat(dayStats.get("start_time")).isEqualTo(14400);
    assertThat(dayStats.get("end_time")).isEqualTo(79200);
    assertThat(dayStats.get("period")).isEqualTo("day");
    assertThat(dayStats.get("period_start_date")).isEqualTo(1457395200L); // 2016-03-08T00:00:00Z
    assertThat(dayStats.get("period_end_date")).isEqualTo(1457481599L); // 2016-03-08T23:59:59Z
    assertThat(dayStats.containsKey("created_date")).isTrue();
    assertThat(dayStats.containsKey("updated_date")).isTrue();
    assertThat(dayStats.size()).isEqualTo(8);
  }

  @Test
  public void shouldNotComputeEmployeeStatsForDayWhenNoPerformanceAvailable() {
    Instant date = Instant.ofEpochSecond(1457395200);

    Map<String, Map> performance = new TreeMap<>();

    Map<String, Object> dayStats = target.compute(performance, date);

    assertThat(dayStats).isNull();
  }
}
