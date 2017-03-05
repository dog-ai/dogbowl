/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase.performance.presence;

import com.clearspring.analytics.util.Lists;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.SortedMap;

import ai.dog.bowl.model.performance.Presence;
import ai.dog.bowl.repository.PresencePerformanceRepository;
import ai.dog.bowl.repository.firebase.FirebaseRestRepository;
import ai.dog.bowl.repository.firebase.performance.PerformanceResponse;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.time.format.DateTimeFormatter.ofPattern;

public class PresencePerformanceFirebaseRestRepository extends FirebaseRestRepository implements PresencePerformanceRepository {

  @Override
  public List<Presence> findAllByDate(String companyId, String employeeId, Instant date) {
    checkArgument(!isNullOrEmpty(companyId));
    checkArgument(!isNullOrEmpty(employeeId));
    checkNotNull(date);

    String formattedDate = ofPattern("yyyy/MM/dd").withZone(ZoneId.of("Z")).format(date);

    String path = "company_employee_performances/" + companyId + "/" + employeeId + "/presence/" + formattedDate;

    PerformanceResponse<Presence> response = (PerformanceResponse<Presence>) client.getValueAsClass(path, false, PerformanceResponse.class);

    if (response == null) {
      return Lists.newArrayList();
    }

    return response.getPerformances();
  }

  @Override
  public Instant findFirstPerformanceDate(String companyId, String employeeId) {
    checkArgument(!isNullOrEmpty(companyId));
    checkArgument(!isNullOrEmpty(employeeId));

    String path = "company_employee_performances/" + companyId + "/" + employeeId + "/presence";

    SortedMap<String, Object> value = (SortedMap<String, Object>) client.getValueAsMap(path, true);

    if (value == null || "null".equals(value)) {
      return null;
    }

    int year = Integer.parseInt(value.firstKey());
    path += "/" + value.firstKey();

    value = (SortedMap<String, Object>) client.getValueAsMap(path, true);

    if (value == null || "null".equals(value)) {
      return null;
    }

    int month = Integer.parseInt(value.firstKey());
    path += "/" + value.firstKey();

    value = (SortedMap<String, Object>) client.getValueAsMap(path, true);

    if (value == null || "null".equals(value)) {
      return null;
    }

    int day = Integer.parseInt(value.firstKey());

    ZonedDateTime date = ZonedDateTime.parse(year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day) + "T00:00:00Z");

    return date.toLocalDate().atStartOfDay().atZone(ZoneId.of("Z")).toInstant();
  }
}
