/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark.rest;

public final class FailedSparkRequestException extends Exception {

    public FailedSparkRequestException(String message) {
        super(message);
    }

    public FailedSparkRequestException(Throwable cause) {
        super(cause);
    }
}
