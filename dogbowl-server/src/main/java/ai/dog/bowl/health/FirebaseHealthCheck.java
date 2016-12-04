/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.health;

import ai.dog.bowl.client.firebase.FirebaseClient;
import com.codahale.metrics.health.HealthCheck;

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