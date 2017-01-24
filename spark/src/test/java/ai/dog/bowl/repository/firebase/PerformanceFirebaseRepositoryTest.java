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

public class PerformanceFirebaseRepositoryTest {

  private PerformanceFirebaseRestRepository target;

  @Before
  public void setUp() {
    target = new PerformanceFirebaseRestRepository();
  }

  @Test
  public void shouldRetrieveEmployeeDayPerformance() {
    String companyId = "-JxUvib222XAWzLpYSP4";
    String employeeId = "-JxUwfPfuf8pSgAwp_xo";
    String performanceName = "presence";
    ZonedDateTime date = now().minus(30, DAYS).atTime(23, 59, 59).atZone(ZoneId.systemDefault());

    Map<String, Map> result = target.findAllByNameAndDate(companyId, employeeId, performanceName, date);

    assertThat(result)
            .isNotNull()
            .isNotEmpty();
  }
}
