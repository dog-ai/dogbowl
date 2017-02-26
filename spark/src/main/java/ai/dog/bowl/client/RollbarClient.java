/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client;

import com.rollbar.Rollbar;

import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.slf4j.LoggerFactory.getLogger;

public class RollbarClient {
  private static final Logger logger = getLogger(RollbarClient.class);

  private Rollbar rollbar;

  public RollbarClient(String apiKey, String environment) {
    checkArgument(!isNullOrEmpty(apiKey));
    checkArgument(!isNullOrEmpty(environment));

    this.rollbar = new Rollbar(apiKey, environment);
    this.rollbar.handleUncaughtErrors();
  }

  public void error(Throwable error) {
    if (this.rollbar == null) {
      return;
    }

    try {
      this.rollbar.error(error);
    } catch (Throwable t) {
      logger.error(t.getMessage());
    }
  }
}
