/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import java.util.Map;

import ai.dog.bowl.repository.DogRepository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.slf4j.LoggerFactory.getLogger;

public class DogFirebaseRestRepository extends FirebaseRestRepository implements DogRepository {
  private static final org.slf4j.Logger logger = getLogger(DogFirebaseRestRepository.class);

  @Override
  public Map<String, Object> findById(String dogId) {
    checkArgument(!isNullOrEmpty(dogId));

    String path = "dogs/" + dogId;

    Map<String, Object> value = client.getValueAsMap(path);

    if (value == null | "null".equals(value)) {
      return null;
    }

    return value;
  }
}
