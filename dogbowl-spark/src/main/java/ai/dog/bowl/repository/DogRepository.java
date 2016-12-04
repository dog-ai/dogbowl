/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository;

import java.util.Map;

public interface DogRepository {
  Map<String, Object> findById(String dogId);
}
