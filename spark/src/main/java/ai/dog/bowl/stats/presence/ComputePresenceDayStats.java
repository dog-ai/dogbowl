/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.stats.presence;

import com.clearspring.analytics.util.Lists;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.dog.bowl.model.performance.Presence;

import static com.google.common.base.Preconditions.checkNotNull;

public class ComputePresenceDayStats {
  public Map<String, Object> compute(List<Presence> presences, Instant date) {
    if (presences == null || presences.isEmpty()) {
      return null;
    }

    Map<String, Object> stats = new HashMap<>();

    int totalDuration = this.computeTotalDuration(presences);
    stats.put("total_duration", totalDuration);

    int startTime = this.computeStartTime(presences);
    stats.put("start_time", startTime);

    int endTime = this.computeEndTime(presences);
    stats.put("end_time", endTime);

    Long now = Instant.now().toEpochMilli() / 1000;
    stats.put("created_date", now);
    stats.put("updated_date", now);
    stats.put("period", "day");
    stats.put("period_start_date", date.atZone(ZoneId.of("Z")).toLocalDate().atStartOfDay(ZoneId.of("Z")).toInstant().toEpochMilli() / 1000);
    stats.put("period_end_date", date.atZone(ZoneId.of("Z")).toLocalDate().atTime(23, 59, 59).atZone(ZoneId.of("Z")).toInstant().toEpochMilli() / 1000);

    return stats;
  }

  private int computeTotalDuration(List<Presence> presences) {
    checkNotNull(presences);

    List<Presence> _presences = Lists.newArrayList();

    for (int i = 0; i < presences.size(); i++) {
      Presence cur = presences.get(i);

      if (cur.isPresent()) {

        if (_presences.size() > 0) {
          Presence prev = _presences.get(_presences.size() - 1);

          if (!prev.isPresent()) {
            _presences.add(cur);
          }

        } else {

          _presences.add(cur);
        }

      } else {

        if (i > 0 && i + 1 < presences.size()) {
          Presence next = presences.get(i + 1);
          boolean longerThan90min = ChronoUnit.MINUTES.between(cur.getCreatedDate(), next.getCreatedDate()) >= 90;
          if (longerThan90min) {
            _presences.add(cur);
          }
        } else {
          _presences.add(cur);
        }

      }
    }

    int totalDuration = 0;
    for (int i = 0; i < _presences.size(); i++) {
      int diff;
      if (_presences.get(i).isPresent()) {
        Instant next;
        if (i + 1 < _presences.size()) {
          next = _presences.get(i + 1).getCreatedDate();
          diff = (int) ChronoUnit.SECONDS.between(_presences.get(i).getCreatedDate(), next);
        } else {
          next = _presences.get(i).getCreatedDate().atZone(ZoneId.of("Z")).toLocalDate().atTime(23, 59, 59).atZone(ZoneId.of("Z")).toInstant();
          diff = (int) ChronoUnit.SECONDS.between(_presences.get(i).getCreatedDate(), next);
        }
        totalDuration += diff;

      } else if (i == 0) {
        // first presence is not present...assume that the first presence is the beginning of the day
        Instant previous = _presences.get(i).getCreatedDate();
        previous = previous.atZone(ZoneId.of("Z")).toLocalDate().atStartOfDay(ZoneId.of("Z")).toInstant();
        diff = (int) ChronoUnit.SECONDS.between(previous, _presences.get(i).getCreatedDate());

        totalDuration += diff;
      }
    }

    return totalDuration;
  }

  private int computeStartTime(List<Presence> presences) {
    checkNotNull(presences);

    if (presences.isEmpty()) {
      return 0;
    }

    Presence first = presences.get(0);
    Instant createdDate = first.isPresent() ? first.getCreatedDate() : startOfDay(first.getCreatedDate());
    Instant startOfDay = startOfDay(createdDate);

    return (int) ChronoUnit.SECONDS.between(startOfDay, createdDate);
  }

  private int computeEndTime(List<Presence> presences) {
    checkNotNull(presences);

    if (presences.isEmpty()) {
      return 0;
    }

    Presence last = presences.get(presences.size() - 1);
    Instant endDate = last.isPresent() ? endOfDay(last.getCreatedDate()) : last.getCreatedDate();
    Instant startOfDay = startOfDay(endDate);

    return (int) ChronoUnit.SECONDS.between(startOfDay, endDate);
  }

  private Instant endOfDay(Instant instant) {
    checkNotNull(instant);

    return instant
            .atZone(ZoneId.of("Z"))
            .toLocalDate().atTime(23, 59, 59)
            .atZone(ZoneId.of("Z"))
            .toInstant();
  }

  private Instant startOfDay(Instant instant) {
    checkNotNull(instant);

    return instant
            .atZone(ZoneId.of("Z"))
            .toLocalDate()
            .atStartOfDay(ZoneId.of("Z"))
            .toInstant();
  }
}
