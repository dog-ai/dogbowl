/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.health;

import ai.dog.bowl.client.spark.SparkClient;
import com.codahale.metrics.health.HealthCheck;

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