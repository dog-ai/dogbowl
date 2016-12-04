/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EmployeeFirebaseRepositoryTest {

  private EmployeeFirebaseRestRepository target;

  @Before
  public void setUp() {
    target = new EmployeeFirebaseRestRepository();
  }

  @Test
  public void shouldFindEmployeesByCompanyId() {
    String companyId = "-JxUvib222XAWzLpYSP4";

    List<String> result = target.findEmployeesByCompanyId(companyId);

    assertThat(result)
            .isNotNull()
            .hasSize(5);
  }
}
