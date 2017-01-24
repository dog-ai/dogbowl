/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.dog.bowl.client.spark.rest.ClusterMode;
import ai.dog.bowl.client.spark.rest.DriverState;
import ai.dog.bowl.client.spark.rest.FailedSparkRequestException;
import ai.dog.bowl.client.spark.rest.JobSubmitRequestSpecification;
import ai.dog.bowl.client.spark.rest.SparkPropertiesSpecification;
import ai.dog.bowl.client.spark.rest.SparkRestClient;
import io.dropwizard.lifecycle.Managed;

public class SparkClient implements Managed {
  private static final Logger logger = LoggerFactory.getLogger(SparkClient.class);

  private final String masterHost;
  private final String resourceUrl;
  private final Map<String, String> environmentVariables = new HashMap<String, String>() {{
    put("DOGBOWL_ENVIRONMENT", System.getenv("DOGBOWL_ENVIRONMENT"));
    put("ROLLBAR_API_KEY", System.getenv("ROLLBAR_API_KEY"));
    put("FIREBASE_PROJECT_ID", System.getenv("FIREBASE_PROJECT_ID"));
    put("FIREBASE_API_KEY", System.getenv("FIREBASE_API_KEY"));
  }};
  private SparkRestClient sparkRestClient;

  public SparkClient(String masterHost, String resourceUrl) {
    this.masterHost = masterHost;
    this.resourceUrl = resourceUrl;
  }

  @Override
  public void start() throws Exception {
    this.sparkRestClient = SparkRestClient.builder()
            .masterHost(this.masterHost)
            .sparkVersion("2.0.2")
            .poolingHttpClient(256)
            .clusterMode(ClusterMode.spark)
            .environmentVariables(environmentVariables)
            .build();
  }

  @Override
  public void stop() throws Exception {
  }

  public String submit(String mainClass, List<String> args) throws FailedSparkRequestException, InterruptedException {
    logger.info("Submitting Spark job with main class " + mainClass);


    JobSubmitRequestSpecification job = sparkRestClient.prepareJobSubmit()
            .appResource(this.resourceUrl)
            .appName(mainClass)
            .mainClass(mainClass)
            .appArgs(args);

    SparkPropertiesSpecification properties = job.withProperties();
    properties.put("spark.submit.deployMode", "cluster");
    for (String key : environmentVariables.keySet()) {
      job.withProperties().put("spark.executorEnv." + key, environmentVariables.get(key));
    }

    String submissionId = job.submit();

    DriverState prevState = null;
    DriverState curState = null;

    try {
      do {

        try {
          curState = sparkRestClient.checkJobStatus()
                  .withSubmissionId(submissionId);
        } catch (FailedSparkRequestException ignored) {
        }

        if (prevState == null || prevState != curState) {

          logger.info(curState.toString().charAt(0) + curState.toString().substring(1).toLowerCase() +
                  " Spark job with submission id " + submissionId);

          switch (curState) {
            case FINISHED:
            case UNKNOWN:
            case KILLED:
            case FAILED:
            case ERROR:
              return curState.toString();
          }
        }

        prevState = curState;

        Thread.sleep(2000);

      } while (true);
    } catch (InterruptedException e) {
      try {
        sparkRestClient.killJob()
                .withSubmissionId(submissionId);

        logger.info("Killed Spark job with submission id " + submissionId);
      } catch (FailedSparkRequestException ignored) {
      }

      throw e;
    }
  }
}
