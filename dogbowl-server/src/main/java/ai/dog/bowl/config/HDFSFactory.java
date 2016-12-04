/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotEmpty;

import ai.dog.bowl.client.hdfs.HDFSClient;
import ai.dog.bowl.health.HDFSHealthCheck;
import io.dropwizard.setup.Environment;

public class HDFSFactory {
  
  @NotEmpty
  @JsonProperty
  private String url;

  @NotEmpty
  @JsonProperty
  private String user;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public HDFSClient build(Environment environment) {
    final HDFSClient client = new HDFSClient(getUrl(), getUser());

    environment.lifecycle().manage(client);
    environment.healthChecks().register("hdfs", new HDFSHealthCheck(client));

    return client;
  }
}
