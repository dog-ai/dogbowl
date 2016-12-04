/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.health;

import com.codahale.metrics.health.HealthCheck;

import ai.dog.bowl.client.hdfs.HDFSClient;

public class HDFSHealthCheck extends HealthCheck {
    private final HDFSClient client;

    public HDFSHealthCheck(HDFSClient client) {
        this.client = client;
    }

    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
