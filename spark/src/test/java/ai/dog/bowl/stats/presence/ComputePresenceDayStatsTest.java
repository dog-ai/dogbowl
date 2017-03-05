/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.stats.presence;

import com.clearspring.analytics.util.Lists;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import ai.dog.bowl.model.performance.Presence;

import static java.time.Instant.ofEpochSecond;
import static org.assertj.core.api.Assertions.assertThat;

public class ComputePresenceDayStatsTest {

  private ComputePresenceDayStats target;

  @Before
  public void setUp() {
    target = new ComputePresenceDayStats();
  }

  @Test
  public void shouldComputeEmployeeStatsForDay() {
    Instant date = ofEpochSecond(1457395200);

    List<Presence> presences = Lists.newArrayList();
    presences.add(new Presence("my-presence-1", ofEpochSecond(1457409600), true));
    presences.add(new Presence("my-presence-2", ofEpochSecond(1457424000), false));
    presences.add(new Presence("my-presence-3", ofEpochSecond(1457467200), true));
    presences.add(new Presence("my-presence-4", ofEpochSecond(1457474400), false));

    Map<String, Object> dayStats = target.compute(presences, date);

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
    Instant date = ofEpochSecond(1457395200);

    List<Presence> presences = Lists.newArrayList();

    Map<String, Object> dayStats = target.compute(presences, date);

    assertThat(dayStats).isNull();
  }
}
