/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client;

import com.rollbar.Rollbar;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class RollbarClient {
  private static final String ENVIRONMENT = System.getenv("DOGBOWL_ENVIRONMENT");
  private static final String ROLLBAR_API_KEY = System.getenv("ROLLBAR_API_KEY");

  private static final Logger logger = getLogger(RollbarClient.class);

  private Rollbar rollbar;

  public RollbarClient() {
    this.rollbar = new Rollbar(ROLLBAR_API_KEY, ENVIRONMENT);
    this.rollbar.handleUncaughtErrors();
  }

  public void error(Throwable error) {
    try {
      this.rollbar.error(error);
    } catch (Throwable t) {
      logger.error(t.getMessage());
    }
  }
}
