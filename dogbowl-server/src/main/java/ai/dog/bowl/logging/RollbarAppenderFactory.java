/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.firebase.client.annotations.NotNull;
import com.tapstream.rollbar.RollbarAppender;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import io.dropwizard.logging.AbstractAppenderFactory;

@JsonTypeName("rollbar")
public class RollbarAppenderFactory extends AbstractAppenderFactory {

  @NotNull
  private String environment = "development";

  private String apiKey;

  @JsonProperty
  public String getEnvironment() {
    return environment;
  }

  @JsonProperty
  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  @JsonProperty
  public String getApiKey() {
    return apiKey;
  }

  @JsonProperty
  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, Layout<ILoggingEvent> layout) {
    final RollbarAppender appender = new RollbarAppender();
    appender.setApiKey(apiKey);
    appender.setEnvironment(environment);
    appender.setContext(context);

    addThresholdFilter(appender, threshold);

    appender.start();

    return wrapAsync(appender);
  }
}
