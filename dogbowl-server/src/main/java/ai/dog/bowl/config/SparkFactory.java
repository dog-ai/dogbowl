/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotEmpty;

import ai.dog.bowl.client.spark.SparkClient;
import ai.dog.bowl.health.SparkHealthCheck;
import io.dropwizard.setup.Environment;

public class SparkFactory {

  @NotEmpty
  @JsonProperty
  private String masterHost;

  public String getMasterHost() {
    return masterHost;
  }

  public void setMasterHost(String masterHost) {
    this.masterHost = masterHost;
  }

  public SparkClient build(Environment environment, String resourceUrl) {
    final SparkClient client = new SparkClient(getMasterHost(), resourceUrl);

    environment.lifecycle().manage(client);
    environment.healthChecks().register("spark", new SparkHealthCheck(client));

    return client;
  }
}
