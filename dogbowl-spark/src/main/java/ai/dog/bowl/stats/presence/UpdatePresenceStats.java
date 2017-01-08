/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.stats.presence;

import org.slf4j.Logger;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.dog.bowl.repository.CompanyRepository;
import ai.dog.bowl.repository.DogRepository;
import ai.dog.bowl.repository.EmployeeRepository;
import ai.dog.bowl.repository.PerformanceRepository;
import ai.dog.bowl.repository.StatsRepository;
import ai.dog.bowl.repository.firebase.CompanyFirebaseRestRepository;
import ai.dog.bowl.repository.firebase.DogFirebaseRestRepository;
import ai.dog.bowl.repository.firebase.EmployeeFirebaseRestRepository;
import ai.dog.bowl.repository.firebase.PerformanceFirebaseRestRepository;
import ai.dog.bowl.repository.firebase.StatsFirebaseRestRepository;

import static java.time.LocalDate.now;
import static java.time.ZonedDateTime.parse;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.slf4j.LoggerFactory.getLogger;

public class UpdatePresenceStats {
  private static final Logger logger = getLogger(UpdatePresenceStats.class);

  private EmployeeRepository employeeRepository;
  private PerformanceRepository performanceRepository;
  private StatsRepository statsRepository;
  private CompanyRepository companyRepository;
  private DogRepository dogRepository;

  private ComputePresenceStats statsUtil;

  public UpdatePresenceStats() {
    this.employeeRepository = new EmployeeFirebaseRestRepository();
    this.performanceRepository = new PerformanceFirebaseRestRepository();
    this.statsRepository = new StatsFirebaseRestRepository();
    this.companyRepository = new CompanyFirebaseRestRepository();
    this.dogRepository = new DogFirebaseRestRepository();

    this.statsUtil = new ComputePresenceStats();
  }
  
  public List<String> getEmployees(String companyId) {
    return this.employeeRepository.findEmployeesByCompanyId(companyId);  
  }

  public void updateEmployeeStats(final String companyId, final String employeeId, final String performanceName) {
    ZonedDateTime periodEndDate = statsRepository
            .retrieveAllTimePeriodEndDate(companyId, employeeId, performanceName);

    if (periodEndDate == null) {
      Map<String, Object> company = companyRepository.findById(companyId);

      if (company == null || !company.containsKey("dog_id") || company.get("dog_id") == null) {
        return;
      }

      String dogId = (String) company.get("dog_id");

      Map<String, Object> dog = dogRepository.findById(dogId);

      if (dog == null || !dog.containsKey("timezone") || dog.get("timezone") == null) {
        return;
      }

      String timezone = (String) dog.get("timezone");

      periodEndDate = performanceRepository.findFirstPerformanceDate(companyId, employeeId, performanceName, timezone);
      periodEndDate = periodEndDate.minus(1, DAYS);
    }

    ZonedDateTime startDate = periodEndDate.plus(1, DAYS);
    ZonedDateTime endDate = now(periodEndDate.getZone())
            .atTime(23, 59, 59)
            .atZone(periodEndDate.getZone());

    updateEmployeeStats(companyId, employeeId, performanceName, startDate, endDate);
  }

  private void updateEmployeeStats(final String companyId, final String employeeId, final String performanceName, final ZonedDateTime startDate, final ZonedDateTime endDate) {
    if (!startDate.isBefore(endDate)) {
      return;
    }

    logger.info("Updating employee presence stats from: " +
            startDate.format(ISO_DATE) + " until " +
            endDate.format(ISO_DATE));

    Map<String, Map<String, Object>> cachedStats = new HashMap<>();
    ZonedDateTime _startDate = startDate;

    while (_startDate.isBefore(endDate)) {
      try {
        Map<String, Map> performance = performanceRepository.findAllByNameAndDate(companyId, employeeId, performanceName, _startDate);
        if (performance == null) {
          statsRepository.updatePeriodEndDate(companyId, employeeId, performanceName, "all-time", _startDate);

          _startDate = _startDate.plus(1, DAYS);

          continue;
        }

        // compute and updateValue day stats
        Map<String, Object> dayStats = statsUtil.computeDayStats(performance, _startDate);
        statsRepository.update(companyId, employeeId, performanceName, "day", _startDate, dayStats);

        // compute and updateValue all other stats periods
        for (String period : new String[]{"month", "year", "all-time"}) {
          if (!cachedStats.containsKey(period)) {
            Map<String, Object> oldStats = statsRepository.retrieve(companyId, employeeId, performanceName, period, _startDate);

            if (oldStats != null && oldStats.containsKey("period_end_date") && (
                    parse((String) oldStats.get("period_end_date"), ISO_ZONED_DATE_TIME).isBefore(_startDate) ||
                            parse((String) oldStats.get("period_end_date"), ISO_ZONED_DATE_TIME).isEqual(_startDate))) {
              cachedStats.put(period, oldStats);
            } else {
              cachedStats.put(period, null);
            }
          }

          Map<String, Object> oldStats = cachedStats.get(period);
          Map<String, Object> newStats = statsUtil.computePeriodStats(dayStats, oldStats, period, _startDate);
          statsRepository.update(companyId, employeeId, performanceName, period, _startDate, newStats);
          cachedStats.put(period, newStats);
        }

        logger.info("Updated presence stats with performance from date: " + _startDate.format(ISO_DATE));

        _startDate = _startDate.plus(1, DAYS);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
