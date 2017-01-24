/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.health;

import com.codahale.metrics.health.HealthCheck;

import ai.dog.bowl.client.spark.SparkClient;

public class SparkHealthCheck extends HealthCheck {
    private final SparkClient client;

    public SparkHealthCheck(SparkClient client) {
        this.client = client;
    }

    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
