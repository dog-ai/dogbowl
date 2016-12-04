/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import java.util.Map;

import ai.dog.bowl.repository.CompanyRepository;

import static org.slf4j.LoggerFactory.getLogger;

public class CompanyFirebaseRestRepository extends FirebaseRepository implements CompanyRepository {
  private static final org.slf4j.Logger logger = getLogger(CompanyFirebaseRestRepository.class);

  @Override
  public Map<String, Object> findById(String companyId) {
    String path = "companies/" + companyId;

    Map<String, Object> value = firebase.getValueAsMap(path);

    if (value == null | "null".equals(value)) {
      return null;
    }

    return value;
  }
}
