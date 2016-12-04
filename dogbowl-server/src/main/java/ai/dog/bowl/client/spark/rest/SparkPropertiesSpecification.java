/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark.rest;

public interface SparkPropertiesSpecification {

    SparkPropertiesSpecification put(String sparkProperty, String value);

    String submit() throws FailedSparkRequestException;

}
