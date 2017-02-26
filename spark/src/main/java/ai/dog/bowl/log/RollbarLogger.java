/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.log;

import ai.dog.bowl.client.RollbarClient;

import static com.google.common.base.Strings.isNullOrEmpty;

public class RollbarLogger {
  private static final String ENVIRONMENT = System.getenv("DOGBOWL_ENVIRONMENT");
  private static final String ROLLBAR_API_KEY = System.getenv("ROLLBAR_API_KEY");

  private RollbarClient rollbar;

  public RollbarLogger() {
    if (isNullOrEmpty(ENVIRONMENT) || isNullOrEmpty(ROLLBAR_API_KEY)) {
      return;
    }

    this.rollbar = new RollbarClient(ROLLBAR_API_KEY, ENVIRONMENT);
  }

  public void error(Throwable error) {
    if (this.rollbar != null) {
      this.rollbar.error(error);
    }
  }
}
