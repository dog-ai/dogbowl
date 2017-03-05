/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.model.performance.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;

public class InstantDeserializer extends JsonDeserializer<Instant> {

  @Override
  public Instant deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
    return Instant.ofEpochSecond(jsonParser.getLongValue());
  }
}
