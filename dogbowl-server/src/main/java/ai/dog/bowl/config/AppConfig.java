/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;

public class AppConfig extends Configuration {

  @Valid
  @NotNull
  @JsonProperty("firebase")
  private FirebaseFactory firebaseFactory = new FirebaseFactory();

  @Valid
  @NotNull
  @JsonProperty("spark")
  private SparkFactory sparkFactory = new SparkFactory();

  @Valid
  @NotNull
  @JsonProperty("hdfs")
  private HDFSFactory hdfsFactory = new HDFSFactory();

  public FirebaseFactory getFirebaseFactory() {
    return firebaseFactory;
  }

  public void setFirebaseFactory(FirebaseFactory firebaseFactory) {
    this.firebaseFactory = firebaseFactory;
  }

  public SparkFactory getSparkFactory() {
    return sparkFactory;
  }

  public void setSparkFactory(SparkFactory sparkFactory) {
    this.sparkFactory = sparkFactory;
  }

  public HDFSFactory getHdfsFactory() {
    return hdfsFactory;
  }

  public void setHdfsFactory(HDFSFactory hdfsFactory) {
    this.hdfsFactory = hdfsFactory;
  }
}
