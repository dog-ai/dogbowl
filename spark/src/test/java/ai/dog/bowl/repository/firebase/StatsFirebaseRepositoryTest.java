/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;

public class StatsFirebaseRepositoryTest {

  private StatsFirebaseRestRepository target;

  @Before
  public void setUp() {
    target = new StatsFirebaseRestRepository();
  }

  @Test
  public void shouldRetrieveEmployeeAllTimePerformanceStatsPeriodEndDate() {
    String companyId = "-JxUvib222XAWzLpYSP4";
    String employeeId = "-JxUwfPfuf8pSgAwp_xo";
    String performanceName = "presence";

    ZonedDateTime result = target.retrieveAllTimePeriodEndDate(companyId, employeeId, performanceName);

    assertThat(result)
            .isNotNull();
  }

  @Test
  public void shouldRetrieveEmployeePeriodPerformanceStats() {
    String companyId = "-JxUvib222XAWzLpYSP4";
    String employeeId = "-JxUwfPfuf8pSgAwp_xo";
    String performanceName = "presence";
    String period = "month";
    ZonedDateTime date = now().minus(30, DAYS).atTime(23, 59, 59).atZone(ZoneId.systemDefault());

    Map<String, Object> result = target.retrieve(companyId, employeeId, performanceName, period, date);

    assertThat(result)
            .isNotNull()
            .isNotEmpty();
  }

  @Test
  public void shouldDeleteAll() {
    String companyId = "-JxUvib222XAWzLpYSP4";
    String employeeId = "-JxUwfPfuf8pSgAwp_xo";
    String performanceName = "presence";

    target.deleteAll(companyId, employeeId, performanceName);
  }
}
