/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark.rest;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
class JobSubmitRequest {

    private Action action;

    private String appResource;

    private List<String> appArgs;

    private String clientSparkVersion;

    private String mainClass;

    private Map<String,String> environmentVariables;

    private SparkProperties sparkProperties;

    public static JobSubmitRequest builder() {
        return new JobSubmitRequest();
    }

    public Action getAction() {
        return action;
    }

    public String getAppResource() {
        return appResource;
    }

    public List<String> getAppArgs() {
        return appArgs;
    }

    public String getClientSparkVersion() {
        return clientSparkVersion;
    }

    public String getMainClass() {
        return mainClass;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public SparkProperties getSparkProperties() {
        return sparkProperties;
    }

    public JobSubmitRequest setAction(Action action) {
        this.action = action;
        return this;
    }

    public JobSubmitRequest setAppResource(String appResource) {
        this.appResource = appResource;
        return this;
    }

    public JobSubmitRequest setAppArgs(List<String> appArgs) {
        this.appArgs = appArgs;
        return this;
    }

    public JobSubmitRequest setClientSparkVersion(String clientSparkVersion) {
        this.clientSparkVersion = clientSparkVersion;
        return this;
    }

    public JobSubmitRequest setMainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public JobSubmitRequest setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
        return this;
    }

    public JobSubmitRequest setSparkProperties(SparkProperties sparkProperties) {
        this.sparkProperties = sparkProperties;
        return this;
    }

    public JobSubmitRequest build() {
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class SparkProperties {

        @JsonProperty(value = "spark.jars")
        private String jars;

        @JsonProperty(value = "spark.app.name")
        private String appName;

        @JsonProperty(value = "spark.master")
        private String master;

        private Map<String,String> otherProperties = new HashMap<>();

        void setOtherProperties(String key, String value) {
            this.otherProperties.put(key,value);
        }

        @JsonAnyGetter
        Map<String,String> getOtherProperties() {
            return this.otherProperties;
        }

        public SparkProperties setJars(String jars) {
            this.jars = jars;
            return this;
        }

        public SparkProperties setAppName(String appName) {
            this.appName = appName;
            return this;
        }

        public SparkProperties setMaster(String master) {
            this.master = master;
            return this;
        }

        public SparkProperties setOtherProperties(Map<String, String> otherProperties) {
            this.otherProperties = otherProperties;
            return this;
        }

        public static SparkProperties builder() {
            return new SparkProperties();
        }

        public SparkProperties build() {
            return this;
        }
    }

}
