/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.job;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;

import java.util.List;

import ai.dog.bowl.log.RollbarLogger;
import ai.dog.bowl.stats.presence.UpdatePresenceStats;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

public class PerformancePresenceStatsUpdate {
  private static final Logger logger = getLogger(PerformancePresenceStatsUpdate.class);
  private static final RollbarLogger rollbar = new RollbarLogger();

  private static final UpdatePresenceStats updatePresenceStats = new UpdatePresenceStats();

  public static void main(String[] args) throws Exception {
    checkArgument(args != null && args.length > 0);
    checkNotNull(args[0]);

    String companyId = args[0];

    logger.info("Started update of presence stats for company " + companyId);

    List<String> employeeIds = updatePresenceStats.findEmployeesByCompanyId(companyId);

    logger.info("Updating " + employeeIds.size() + " employees: " + employeeIds);

    SparkSession session = SparkSession
            .builder()
            .appName("PerformancePresenceUpdateStats")
            .getOrCreate();

    JavaSparkContext context = new JavaSparkContext(session.sparkContext());

    context.parallelize(employeeIds)
      .foreach(employeeId -> {
        try {
          logger.info("Started update of presence stats for employee " + employeeId);

          updatePresenceStats.updateEmployeeStats(companyId, employeeId);

        } catch (Throwable error) {
          logger.error(error.getMessage());

          rollbar.error(error);
        } finally {
          logger.info("Finished update of presence stats for employee " + employeeId);
        }
      });

    context.stop();

    logger.info("Finished update of presence stats for company " + companyId);
  }
}
