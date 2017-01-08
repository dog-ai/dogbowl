/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.hdfs;

import com.google.common.io.ByteStreams;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;

import io.dropwizard.lifecycle.Managed;

public class HDFSClient implements Managed {
  private static final Logger logger = LoggerFactory.getLogger(HDFSClient.class);

  private final String url;
  private FileSystem hdfs;

  public HDFSClient(String url, String user) {
    this.url = url;

    System.setProperty("HADOOP_USER_NAME", user);
  }

  @Override
  public void start() throws Exception {
    Configuration configuration = new Configuration();
    configuration.set("fs.defaultFS", url);
    configuration.set("dfs.client.use.datanode.hostname", "true");
    configuration.set("dfs.replication", "1");

    hdfs = FileSystem.get(configuration);
  }

  @Override
  public void stop() throws Exception {
    hdfs = null;
  }

  public void uploadJar(String jar, String dest) throws Exception {
    if (hdfs == null) {
      throw new IllegalStateException("HDFS client has not been started");
    }

    logger.info("Uploading " + jar + " to " + url + dest);
    Path file = new Path(url + dest);

    if (hdfs.exists(file)) {
      hdfs.delete(file, true);
    }

    OutputStream os = hdfs.create(file);

    InputStream is = this.getClass().getClassLoader().getResourceAsStream(jar);
    if (is == null) {
      throw new RuntimeException(jar + " not found");
    }

    ByteStreams.copy(is, os);

    hdfs.close();
  }
}
