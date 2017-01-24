/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl;

import com.google.common.base.Throwables;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.dog.bowl.client.HDFSClient;
import ai.dog.bowl.client.firebase.FirebaseClient;
import ai.dog.bowl.client.firebase.queue.FirebaseQueue;
import ai.dog.bowl.client.firebase.queue.TaskProcessor;
import ai.dog.bowl.client.spark.SparkClient;
import ai.dog.bowl.config.AppConfig;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public class App extends Application<AppConfig> {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(App.class);

  private static final String JAR_FILENAME = "spark.jar";
  private final Map<String, FirebaseQueue> queues = new HashMap<>();
  private FirebaseClient firebaseClient;
  private SparkClient sparkClient;
  private HDFSClient hdfsClient;
  private TaskProcessor processor = task -> {
    Map<String, Object> data = task.getData();

    if (!data.containsKey("event") ||
            !data.containsKey("data") || !(data.get("data") instanceof ArrayList)) {
      throw new InvalidParameterException("Invalid task");
    }

    String mainClass = LOWER_UNDERSCORE.to(UPPER_CAMEL, ((String) data.get("event")).replace(":", "_"));
    List<String> args = (List) data.get("data");

    try {
      String result = sparkClient.submit("ai.dog.bowl.job." + mainClass, args);

      if ("FINISHED".equals(result)) {
        task.resolve();
      } else {
        task.reject(result);
      }
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      logger.error(e.getMessage(), e);

      task.reject(Throwables.getRootCause(e));
    }
  };

  public static void main(String[] args) throws Exception {
    new App().run(args);
  }

  @Override
  public void initialize(Bootstrap<AppConfig> bootstrap) {
    // Enable variable substitution with environment variables
    bootstrap.setConfigurationSourceProvider(
            new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                    new EnvironmentVariableSubstitutor()
            )
    );
  }

  @Override
  public String getName() {
    return "dogbowl";
  }

  @Override
  public void run(AppConfig config, Environment environment) throws Exception {
    environment.jersey().disable();

    firebaseClient = config.getFirebaseFactory().build(environment);
    hdfsClient = config.getHdfsFactory().build(environment);

    String resourceUrl = config.getHdfsFactory().getUrl() + "/.tmp/spark/" + JAR_FILENAME;
    sparkClient = config.getSparkFactory().build(environment, resourceUrl);

    environment.lifecycle().manage(new Managed() {
      @Override
      public void start() throws Exception {
        hdfsClient.uploadJar(JAR_FILENAME, "/.tmp/spark/" + JAR_FILENAME);

        firebaseClient.getFirebase().child("_info/companies").addChildEventListener(new ChildEventListener() {

          public void onChildAdded(DataSnapshot snapshot, String s) {
            createQueue(snapshot.getKey());
          }

          public void onChildChanged(DataSnapshot snapshot, String s) {
          }

          public void onChildRemoved(DataSnapshot snapshot) {
            destroyQueue(snapshot.getKey());
          }

          public void onChildMoved(DataSnapshot snapshot, String s) {
          }

          public void onCancelled(FirebaseError error) {
            logger.error(error.getMessage());
          }
        });
      }

      @Override
      public void stop() throws Exception {
        for (String company : queues.keySet()) {
          destroyQueue(company);
        }
      }
    });
  }

  private void createQueue(String s) {
    logger.info("Creating queue for /companies/" + s + "/tasks");

    FirebaseQueue queue = new FirebaseQueue.Builder(firebaseClient.getFirebase(), processor)
            .taskChild("/companies/" + s + "/tasks")
            .specChild("/queue/specs")
            .specId("compute_spec")
            .numWorkers(1)
            .suppressStack(true)
            .build();

    queues.put(s, queue);
  }

  private void destroyQueue(String s) {
    logger.info("Destroying queue for /companies/" + s + "/tasks");

    if (queues.containsKey(s)) {
      FirebaseQueue queue = queues.remove(s);

      queue.shutdown();
    }
  }
}
