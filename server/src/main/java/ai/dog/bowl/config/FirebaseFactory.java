/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotEmpty;

import ai.dog.bowl.client.firebase.FirebaseClient;
import ai.dog.bowl.health.FirebaseHealthCheck;
import io.dropwizard.setup.Environment;

public class FirebaseFactory {

  @NotEmpty
  @JsonProperty
  private String url;

  @NotEmpty
  @JsonProperty("apiKey")
  private String apiKey;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public FirebaseClient build(Environment environment) {
    final FirebaseClient client = new FirebaseClient(getUrl(), getApiKey());

    environment.lifecycle().manage(client);
    environment.healthChecks().register("firebase", new FirebaseHealthCheck(client));

    return client;
  }
}
