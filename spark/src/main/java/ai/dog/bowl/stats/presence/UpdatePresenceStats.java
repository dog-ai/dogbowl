/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.stats.presence;

import org.slf4j.Logger;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import ai.dog.bowl.repository.CompanyRepository;
import ai.dog.bowl.repository.EmployeeRepository;
import ai.dog.bowl.repository.PerformanceRepository;
import ai.dog.bowl.repository.StatsRepository;
import ai.dog.bowl.repository.firebase.CompanyFirebaseRestRepository;
import ai.dog.bowl.repository.firebase.EmployeeFirebaseRestRepository;
import ai.dog.bowl.repository.firebase.PerformanceFirebaseRestRepository;
import ai.dog.bowl.repository.firebase.StatsFirebaseRestRepository;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.slf4j.LoggerFactory.getLogger;

public class UpdatePresenceStats {
  private static final Logger logger = getLogger(UpdatePresenceStats.class);

  private EmployeeRepository employeeRepository;
  private PerformanceRepository performanceRepository;
  private StatsRepository statsRepository;
  private CompanyRepository companyRepository;

  private ComputeDayStats computeDayStats;
  private ComputePeriodStats computePeriodStats;

  public UpdatePresenceStats() {
    this.employeeRepository = new EmployeeFirebaseRestRepository();
    this.performanceRepository = new PerformanceFirebaseRestRepository();
    this.statsRepository = new StatsFirebaseRestRepository();
    this.companyRepository = new CompanyFirebaseRestRepository();

    this.computeDayStats = new ComputeDayStats();
    this.computePeriodStats = new ComputePeriodStats();
  }

  public List<String> findEmployeesByCompanyId(String companyId) {
    return this.employeeRepository.findEmployeesByCompanyId(companyId);  
  }

  public void updateEmployeeStats(final String companyId, final String employeeId, final String performanceName) {
    Instant periodEndDate = statsRepository.retrieveAllTimePeriodEndDate(companyId, employeeId, performanceName);

    if (periodEndDate == null) {
      Map<String, Object> company = companyRepository.findById(companyId);

      if (company == null || !company.containsKey("dog_id") || company.get("dog_id") == null) {
        return;
      }

      periodEndDate = performanceRepository.findFirstPerformanceDate(companyId, employeeId, performanceName);
      if (periodEndDate == null) {
        return;
      }

      statsRepository.deleteAll(companyId, employeeId, performanceName);

      periodEndDate = periodEndDate.minus(1, DAYS);
    }

    Instant updatedDate = statsRepository.retrieveAllTimeUpdatedDate(companyId, employeeId, performanceName);
    if (updatedDate != null && periodEndDate.isBefore(updatedDate) && periodEndDate.until(updatedDate, DAYS) > 40) {
      periodEndDate = updatedDate.atZone(ZoneId.of("Z")).toLocalDate()
              .atTime(23, 59, 59)
              .atZone(ZoneId.of("Z")).toInstant();
    }

    Instant startDate = periodEndDate.plus(1, DAYS);
    Instant endDate = Instant.now().atZone(ZoneId.of("Z")).toLocalDate()
            .atTime(23, 59, 59)
            .atZone(ZoneId.of("Z")).toInstant();

    updateEmployeeStats(companyId, employeeId, performanceName, startDate, endDate);
  }

  private void updateEmployeeStats(final String companyId, final String employeeId, final String performanceName, final Instant startDate, final Instant endDate) {
    if (!startDate.isBefore(endDate)) {
      return;
    }

    logger.info("Updating employee presence stats from: " + startDate.atZone(ZoneId.of("Z")) + " until " + endDate.atZone(ZoneId.of("Z")));

    Instant currentDate = startDate;

    while (currentDate.isBefore(endDate)) {
      try {
        Map<String, Map> performance = performanceRepository.findAllByNameAndDate(companyId, employeeId, performanceName, currentDate);
        if (performance == null) {
          statsRepository.updatePeriodEndDate(companyId, employeeId, performanceName, "all-time", currentDate, currentDate.toEpochMilli() / 1000);

          currentDate = currentDate.plus(1, DAYS);

          continue;
        }

        Map<String, Object> dayStats = updateEmployeeDayStats(companyId, employeeId, performanceName, performance, currentDate);

        // compute and update all other stats periods
        for (String period : new String[]{"month", "year", "all-time"}) {
          updateEmployeePeriodStats(companyId, employeeId, performanceName, period, dayStats, currentDate);
        }

        logger.info("Updated presence stats with performance from date: " + currentDate);

        currentDate = currentDate.plus(1, DAYS);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private Map<String, Object> updateEmployeeDayStats(final String companyId, final String employeeId, final String performanceName, Map<String, Map> performance, Instant date) {
    Map<String, Object> dayStats = computeDayStats.compute(performance, date);

    statsRepository.update(companyId, employeeId, performanceName, "day", date, dayStats);

    return dayStats;
  }

  private void updateEmployeePeriodStats(final String companyId, final String employeeId, final String performanceName, String period, Map<String, Object> dayStats, Instant date) {
    Map<String, Object> oldStats = statsRepository.retrieve(companyId, employeeId, performanceName, period, date);
    Map<String, Object> newStats = computePeriodStats.compute(dayStats, oldStats, period, date);

    statsRepository.update(companyId, employeeId, performanceName, period, date, newStats);
  }
}
