/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase.performance;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

import ai.dog.bowl.model.performance.Performance;
import ai.dog.bowl.repository.firebase.performance.presence.json.PresencePerformanceResponseDeserializer;

@JsonDeserialize(using = PresencePerformanceResponseDeserializer.class)
public class PerformanceResponse<T extends Performance> {
  private List<T> performances;

  public List<T> getPerformances() {
    return performances;
  }

  public void setPerformances(List<T> performances) {
    this.performances = performances;
  }
}
