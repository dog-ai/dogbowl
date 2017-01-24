/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import ai.dog.bowl.client.spark.rest.JobSubmitRequestSpecification;
import ai.dog.bowl.client.spark.rest.SparkRestClient;
import ai.dog.bowl.client.spark.rest.SparkRestClient.SparkRestClientBuilder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SparkClientTest {
  private final SparkClient target = new SparkClient("my-master-host", "my-resource-url");

  private final SparkRestClient sparkRestClient = mock(SparkRestClient.class);
  private final SparkRestClientBuilder sparkRestClientBuilder = mock(SparkRestClientBuilder.class);
  private final JobSubmitRequestSpecification jobSubmitRequestSpecification = mock(JobSubmitRequestSpecification.class);

  @Before
  public void setup() throws Exception {
    when(SparkRestClient.builder()).thenReturn(sparkRestClientBuilder);
    when(sparkRestClientBuilder.build()).thenReturn(sparkRestClient);
    when(sparkRestClient.prepareJobSubmit()).thenReturn(jobSubmitRequestSpecification);
  }

  @Test
  public void shouldSubmit() throws Exception {
    String mainClass = "my-main-class";
    List<String> args = Lists.newArrayList();

    target.submit(mainClass, args);

    verify(jobSubmitRequestSpecification).appName(mainClass);
    verify(jobSubmitRequestSpecification).mainClass(mainClass);
    verify(jobSubmitRequestSpecification).appArgs(args);
  }
}
