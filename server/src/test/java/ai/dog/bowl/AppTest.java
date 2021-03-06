/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl;

import org.junit.Before;
import org.junit.Test;

import ai.dog.bowl.client.firebase.FirebaseClient;
import ai.dog.bowl.client.spark.SparkClient;
import ai.dog.bowl.config.AppConfig;
import ai.dog.bowl.config.FirebaseFactory;
import ai.dog.bowl.config.HDFSFactory;
import ai.dog.bowl.config.SparkFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AppTest {
  private final App target = new App();

  private final Environment environment = mock(Environment.class);
  private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
  private final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
  private final AppConfig config = mock(AppConfig.class);
  private final FirebaseFactory firebaseFactory = mock(FirebaseFactory.class);
  private final FirebaseClient firebaseClient = mock(FirebaseClient.class);
  private final HDFSFactory hdfsFactory = mock(HDFSFactory.class);
  private final SparkFactory sparkFactory = mock(SparkFactory.class);
  private final SparkClient sparkClient = mock(SparkClient.class);

  @Before
  public void setup() throws Exception {
    when(environment.jersey()).thenReturn(jerseyEnvironment);
    when(environment.lifecycle()).thenReturn(lifecycleEnvironment);

    when(config.getFirebaseFactory()).thenReturn(firebaseFactory);
    when(config.getHdfsFactory()).thenReturn(hdfsFactory);
    when(config.getSparkFactory()).thenReturn(sparkFactory);

    when(sparkFactory.build(eq(environment), anyString())).thenReturn(sparkClient);
    when(firebaseFactory.build(environment)).thenReturn(firebaseClient);
  }

  @Test
  public void shouldBuildFirebaseClient() throws Exception {
    target.run(config, environment);

    verify(firebaseFactory).build(environment);
  }

  @Test
  public void shouldBuildHDFSClient() throws Exception {
    target.run(config, environment);

    verify(hdfsFactory).build(environment);
  }

  @Test
  public void shouldBuildSparkClient() throws Exception {
    target.run(config, environment);

    verify(sparkFactory).build(eq(environment), anyString());
  }
}
