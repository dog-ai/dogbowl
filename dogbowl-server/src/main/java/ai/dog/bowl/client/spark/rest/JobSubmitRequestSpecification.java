/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark.rest;

import java.util.List;
import java.util.Set;

public interface JobSubmitRequestSpecification {

    JobSubmitRequestSpecification appResource(String appResource);

    JobSubmitRequestSpecification appArgs(List<String> appArgs);

    JobSubmitRequestSpecification mainClass(String mainClass);

    JobSubmitRequestSpecification appName(String appName);

    JobSubmitRequestSpecification usingJars(Set<String> jars);

    SparkPropertiesSpecification withProperties();

    String submit() throws FailedSparkRequestException;

}
