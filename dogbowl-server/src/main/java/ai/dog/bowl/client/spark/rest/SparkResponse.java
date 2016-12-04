/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class SparkResponse {

    SparkResponse() {}

    private Action action;

    protected String message;

    protected String serverSparkVersion;

    protected String submissionId;

    protected Boolean success;

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
