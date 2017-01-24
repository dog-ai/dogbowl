/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class SparkResponse {

    protected String message;
    protected String serverSparkVersion;
    protected String submissionId;
    protected Boolean success;
  private Action action;

  SparkResponse() {
  }

    public Action getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public String getServerSparkVersion() {
        return serverSparkVersion;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public Boolean getSuccess() {
        return success;
    }
}
