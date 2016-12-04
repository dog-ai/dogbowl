/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark.rest;

public interface RequestOptionsSpecification {
    JobSubmitRequestSpecification prepareJobSubmit();
    KillJobRequestSpecification killJob();
    JobStatusRequestSpecification checkJobStatus();
}
