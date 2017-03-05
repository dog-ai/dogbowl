/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase.performance.presence.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ai.dog.bowl.model.performance.Presence;
import ai.dog.bowl.repository.firebase.performance.PerformanceResponse;

public class PresencePerformanceResponseDeserializer extends JsonDeserializer<PerformanceResponse<Presence>> {

  @Override
  public PerformanceResponse<Presence> deserialize(JsonParser jsonParser, DeserializationContext context)
          throws IOException {
    JsonNode node = jsonParser.readValueAsTree();

    List<Presence> performances = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();

    for (Iterator<Map.Entry<String, JsonNode>> fields = node.fields(); fields.hasNext(); ) {
      Map.Entry<String, JsonNode> next = fields.next();
      if (next.getKey().charAt(0) == '_') {
        continue;
      }

      Presence performance = mapper.readValue(next.getValue().toString(), Presence.class);
      performance.setId(next.getKey());
      performances.add(performance);
    }

    PerformanceResponse<Presence> response = new PerformanceResponse<>();
    response.setPerformances(performances);

    return response;
  }
}
