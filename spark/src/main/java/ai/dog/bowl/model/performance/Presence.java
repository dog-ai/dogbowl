/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.model.performance;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class Presence extends Performance {
  @JsonProperty("is_present")
  private boolean isPresent;

  public Presence() {
    super("presence");
  }

  public Presence(String id, Instant createdDate, boolean isPresent) {
    super("presence");

    this.id = id;
    this.createdDate = createdDate;
    this.isPresent = isPresent;
  }

  public boolean isPresent() {
    return isPresent;
  }

  public void setPresent(boolean present) {
    isPresent = present;
  }
}
