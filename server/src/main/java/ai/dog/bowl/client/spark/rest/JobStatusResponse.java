/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark.rest;

class JobStatusResponse extends SparkResponse {

    private DriverState driverState;
    private String workerHostPort;
    private String workerId;

  JobStatusResponse() {
  }

    public DriverState getDriverState() {
        return driverState;
    }

    public String getWorkerHostPort() {
        return workerHostPort;
    }

    public String getWorkerId() {
        return workerId;
    }
}
