/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.spark.rest;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.Collections;
import java.util.Map;

public class SparkRestClient implements RequestOptionsSpecification {

  private static final String DEPLOY_MODE_CLUSTER = "cluster";
    private String sparkVersion;

    private Integer masterPort;

    private String masterHost;

    private ClusterMode clusterMode;

    private Map<String,String> environmentVariables;
    private HttpClient client;

  SparkRestClient() {
  }

    public static SparkRestClientBuilder builder() {
        return new SparkRestClientBuilder();
    }

  String getMasterUrl() {
    return masterHost + ":" + masterPort;
  }

    @Override
    public JobSubmitRequestSpecification prepareJobSubmit() {
        return new JobSubmitRequestSpecificationImpl(this);
    }

    @Override
    public KillJobRequestSpecification killJob() {
        return new KillJobRequestSpecificationImpl(this);
    }

    @Override
    public JobStatusRequestSpecification checkJobStatus() {
        return new JobStatusRequestSpecificationImpl(this);
    }

    public HttpClient getClient() {
        return client;
    }

  public void setClient(HttpClient client) {
    this.client = client;
  }

    public String getSparkVersion() {
        return sparkVersion;
    }

  public void setSparkVersion(String sparkVersion) {
    this.sparkVersion = sparkVersion;
  }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

  public void setEnvironmentVariables(Map<String, String> environmentVariables) {
    this.environmentVariables = environmentVariables;
  }

    public ClusterMode getClusterMode() {
        return clusterMode;
    }

  public void setClusterMode(ClusterMode clusterMode) {
    this.clusterMode = clusterMode;
    }

    public void setMasterPort(Integer masterPort) {
        this.masterPort = masterPort;
    }

    public String getMasterHost() {
        return masterHost;
    }

    public void setMasterHost(String masterHost) {
        this.masterHost = masterHost;
    }

    public static class SparkRestClientBuilder {
        private String sparkVersion;
        private Integer masterPort = 6066;
        private String masterHost;
        private ClusterMode clusterMode = ClusterMode.spark;

        private Map<String,String> environmentVariables = Collections.emptyMap();

        private HttpClient client = HttpClientBuilder.create()
                .setConnectionManager(new BasicHttpClientConnectionManager())
                .build();

        private SparkRestClientBuilder() {
        }

        public SparkRestClientBuilder sparkVersion(String sparkVersion) {
            this.sparkVersion = sparkVersion;
            return this;
        }

        public SparkRestClientBuilder masterPort(Integer masterPort) {
            this.masterPort = masterPort;
            return this;
        }

        public SparkRestClientBuilder masterHost(String masterHost) {
            this.masterHost = masterHost;
            return this;
        }

        public SparkRestClientBuilder clusterMode(ClusterMode clusterMode) {
            this.clusterMode = clusterMode;
            return this;
        }

        public SparkRestClientBuilder environmentVariables(Map<String, String> environmentVariables) {
            this.environmentVariables = environmentVariables;
            return this;
        }

        public SparkRestClientBuilder httpClient(HttpClient httpClient) {
            this.client = httpClient;
            return this;
        }

        public SparkRestClientBuilder poolingHttpClient(int maxTotalConnections) {
            final PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
            poolingHttpClientConnectionManager.setMaxTotal(maxTotalConnections);
            poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxTotalConnections); // we will have only one route - spark master
            this.client = HttpClientBuilder.create().setConnectionManager(poolingHttpClientConnectionManager).build();
            return this;
        }

        public SparkRestClient build() {
            if (masterHost == null ||
              masterPort == null) {
                throw new IllegalArgumentException("master host and port must be set.");
            }
            if (client == null) {
                throw new IllegalArgumentException("http client cannot be null.");
            }
            if (sparkVersion == null || sparkVersion.isEmpty()) {
                throw new IllegalArgumentException("spark version is not set.");
            }
            SparkRestClient sparkRestClient = new SparkRestClient();
            sparkRestClient.setSparkVersion(sparkVersion);
            sparkRestClient.setMasterPort(masterPort);
            sparkRestClient.setMasterHost(masterHost);
            sparkRestClient.setEnvironmentVariables(environmentVariables);
            sparkRestClient.setClient(client);
            sparkRestClient.setClusterMode(clusterMode);
            return sparkRestClient;
        }
    }
}
