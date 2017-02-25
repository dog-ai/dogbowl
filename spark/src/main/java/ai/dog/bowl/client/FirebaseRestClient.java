/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import net.jodah.failsafe.RetryPolicy;

import java.util.Map;
import java.util.SortedMap;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static net.jodah.failsafe.Failsafe.with;

public class FirebaseRestClient {
  private static final String AUTH_PARAM_NAME = "auth";
  private static final String PATH_FORMAT = "%s.json";

  private final Client client;

  private final RetryPolicy retryPolicy;

  private final String url;
  private final String token;
  
  public FirebaseRestClient(String url, String token) {
    this.url = url;
    this.token = token;

    ClientConfig config = new DefaultClientConfig();
    config.getClasses().add(JacksonJsonProvider.class);
    this.client = Client.create(config);

    this.retryPolicy = new RetryPolicy()
            .retryIf((ClientResponse response) -> response.getStatus() >= 500)
            .withBackoff(4, 32, SECONDS)
            .withMaxRetries(5);
  }

  public void setValue(String path, Map value) {
    with(retryPolicy).run(() -> client.resource(url)
            .path(String.format(PATH_FORMAT, path))
            .queryParam(AUTH_PARAM_NAME, token)
            .type(APPLICATION_JSON).entity(value)
            .put(SortedMap.class));
  }

  public void updateValue(String path, Map value) {
    with(retryPolicy).run(() -> client.resource(url)
            .path(String.format(PATH_FORMAT, path))
            .queryParam(AUTH_PARAM_NAME, token)
            .header("X-HTTP-Method-Override", "PATCH")
            .type(APPLICATION_JSON).entity(value)
            .post(Map.class));
  }

  public Map getValueAsMap(String path) {
    return getValueAsMap(path, false);
  }

  public Map getValueAsMap(String path, Boolean shallow) {
    return client.resource(url)
            .path(String.format(PATH_FORMAT, path))
            .queryParam(AUTH_PARAM_NAME, token)
            .queryParam("shallow", shallow.toString())
            .get(SortedMap.class);
  }

  public String getValueAsString(String path) {
    return client.resource(url)
            .path(String.format(PATH_FORMAT, path))
            .queryParam(AUTH_PARAM_NAME, token)
            .get(String.class);
  }

  public void deleteValue(String path) {
    with(retryPolicy).run(() -> client.resource(url)
            .path(String.format(PATH_FORMAT, path))
            .queryParam(AUTH_PARAM_NAME, token)
            .delete(String.class));
  }
}