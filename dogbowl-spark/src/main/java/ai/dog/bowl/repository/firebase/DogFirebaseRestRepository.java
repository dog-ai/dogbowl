/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import java.util.Map;

import ai.dog.bowl.repository.DogRepository;

import static org.slf4j.LoggerFactory.getLogger;

public class DogFirebaseRestRepository extends FirebaseRepository implements DogRepository {
  private static final org.slf4j.Logger logger = getLogger(DogFirebaseRestRepository.class);

  @Override
  public Map<String, Object> findById(String dogId) {
    String path = "dogs/" + dogId;

    Map<String, Object> value = firebase.getValueAsMap(path);

    if (value == null | "null".equals(value)) {
      return null;
    }

    return value;
  }
}
