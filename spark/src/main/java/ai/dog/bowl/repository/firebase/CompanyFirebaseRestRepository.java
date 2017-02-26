/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import java.util.Map;

import ai.dog.bowl.repository.CompanyRepository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.slf4j.LoggerFactory.getLogger;

public class CompanyFirebaseRestRepository extends FirebaseRestRepository implements CompanyRepository {
  private static final org.slf4j.Logger logger = getLogger(CompanyFirebaseRestRepository.class);

  @Override
  public Map<String, Object> findById(String companyId) {
    checkArgument(!isNullOrEmpty(companyId));

    String path = "companies/" + companyId;

    Map<String, Object> value = client.getValueAsMap(path);

    if (value == null | "null".equals(value)) {
      return null;
    }

    return value;
  }
}
