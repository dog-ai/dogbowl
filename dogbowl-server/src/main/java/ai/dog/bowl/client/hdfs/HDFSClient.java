/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
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

  private static final String JAR_FILENAME = "dogbowl-spark.jar";

  private final String url;

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

    FileSystem hdfs = FileSystem.get(configuration);
    Path file = new Path(url + "/.tmp/spark/" + JAR_FILENAME);

    if (hdfs.exists(file)) {
      hdfs.delete(file, true);
    }

    OutputStream os = hdfs.create(file);
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(JAR_FILENAME);
    ByteStreams.copy(is, os);

    hdfs.close();
  }

  @Override
  public void stop() throws Exception {
  }
}
