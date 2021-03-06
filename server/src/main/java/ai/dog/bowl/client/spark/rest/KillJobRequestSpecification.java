/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark.rest;

public interface KillJobRequestSpecification {
    boolean withSubmissionId(String submissionId) throws FailedSparkRequestException;
}
