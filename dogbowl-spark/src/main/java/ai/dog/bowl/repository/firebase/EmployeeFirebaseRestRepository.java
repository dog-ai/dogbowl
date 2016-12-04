/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.dog.bowl.repository.EmployeeRepository;

import static org.slf4j.LoggerFactory.getLogger;

public class EmployeeFirebaseRestRepository extends FirebaseRepository implements EmployeeRepository {
  private static final org.slf4j.Logger logger = getLogger(EmployeeFirebaseRestRepository.class);

  public List<String> findEmployeesByCompanyId(String companyId) {
    logger.debug("Started retrieve employees: " + companyId);

    String path = "companies/" + companyId + "/employees";
    Map value = firebase.getValueAsMap(path);

    if (value == null) {
      return null;
    }

    List<String> employees = new ArrayList<>();
    employees.addAll(value.keySet());

    return employees;
  }
}
