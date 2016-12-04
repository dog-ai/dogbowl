/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark.rest;

public interface CanValidateSubmissionId {
    default void assertSubmissionId(final String submissionId) {
        if (submissionId == null
                || submissionId.isEmpty()
                || submissionId.trim().equals("")) {
            throw new IllegalArgumentException("SubmissionId must be a non blank string");
        }
    }
}
