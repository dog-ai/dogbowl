/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.dog.bowl.repository.EmployeeRepository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.slf4j.LoggerFactory.getLogger;

public class EmployeeFirebaseRestRepository extends FirebaseRestRepository implements EmployeeRepository {
  private static final Logger logger = getLogger(EmployeeFirebaseRestRepository.class);

  public List<String> findEmployeesByCompanyId(String companyId) {
    checkArgument(!isNullOrEmpty(companyId));

    logger.debug("Started retrieve employees: " + companyId);

    String path = "companies/" + companyId + "/employees";
    Map value = client.getValueAsMap(path);

    if (value == null) {
      return new ArrayList<>();
    }

    List<String> employees = new ArrayList<>();
    employees.addAll(value.keySet());

    return employees;
  }
}
