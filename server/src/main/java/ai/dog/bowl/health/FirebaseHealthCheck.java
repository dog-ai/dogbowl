/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.health;

import com.codahale.metrics.health.HealthCheck;

import ai.dog.bowl.client.firebase.FirebaseClient;

public class FirebaseHealthCheck extends HealthCheck {
    private final FirebaseClient client;

    public FirebaseHealthCheck(FirebaseClient client) {
        this.client = client;
    }

    @Override
    protected Result check() throws Exception {
        if (client.isConnected()) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Disconnected from " + client.getUrl());
        }
    }
}
