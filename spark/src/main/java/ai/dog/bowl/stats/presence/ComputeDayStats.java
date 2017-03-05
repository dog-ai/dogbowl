/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.stats.presence;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ComputeDayStats {
  public Map<String, Object> compute(Map<String, Map> performance, Instant date) {
    if (performance == null || performance.isEmpty()) {
      return null;
    }

    Map<String, Object> stats = new HashMap<>();

    int totalDuration = this.computeTotalDuration(performance);
    stats.put("total_duration", totalDuration);

    int startTime = this.computeStartTime(performance);
    stats.put("start_time", startTime);

    int endTime = this.computeEndTime(performance);
    stats.put("end_time", endTime);

    Long now = Instant.now().toEpochMilli() / 1000;
    stats.put("created_date", now);
    stats.put("updated_date", now);
    stats.put("period", "day");
    stats.put("period_start_date", date.atZone(ZoneId.of("Z")).toLocalDate().atStartOfDay(ZoneId.of("Z")).toInstant().toEpochMilli() / 1000);
    stats.put("period_end_date", date.atZone(ZoneId.of("Z")).toLocalDate().atTime(23, 59, 59).atZone(ZoneId.of("Z")).toInstant().toEpochMilli() / 1000);

    return stats;
  }

  private int computeTotalDuration(Map<String, Map> performance) {
    checkNotNull(performance);

    ArrayList<Map<String, Object>> _presences = new ArrayList<>();

    String[] keys = performance.keySet().toArray(new String[performance.keySet().size()]);

    for (int i = 0; i < keys.length; i++) {
      Map<String, Object> cur = (Map<String, Object>) performance.get(keys[i]);

      if ((Boolean) cur.get("is_present")) {

        if (_presences.size() > 0) {
          Map<String, Object> prev = _presences.get(_presences.size() - 1);

          if (!((Boolean) prev.get("is_present"))) {
            _presences.add(cur);
          }

        } else {

          _presences.add(cur);
        }

      } else {

        if (i > 0 && i + 1 < keys.length) {
          Map<String, Object> next = (Map<String, Object>) performance.get(keys[i + 1]);
          Instant nextCreatedDate = Instant.ofEpochSecond((Integer) next.get("created_date"));
          Instant curCreatedDate = Instant.ofEpochSecond((Integer) cur.get("created_date"));
          boolean longerThan90min = ChronoUnit.MINUTES.between(curCreatedDate, nextCreatedDate) >= 90;
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
      if ((Boolean) _presences.get(i).get("is_present")) {
        Instant next;
        if (i + 1 < _presences.size()) {
          next = Instant.ofEpochSecond((Integer) _presences.get(i + 1).get("created_date"));
          diff = (int) ChronoUnit.SECONDS.between(Instant.ofEpochSecond((Integer) _presences.get(i).get("created_date")), next);
        } else {
          next = Instant.ofEpochSecond((Integer) _presences.get(i).get("created_date")).atZone(ZoneId.of("Z")).toLocalDate().atTime(23, 59, 59).atZone(ZoneId.of("Z")).toInstant();
          diff = (int) ChronoUnit.SECONDS.between(Instant.ofEpochSecond((Integer) _presences.get(i).get("created_date")), next);
        }
        totalDuration += diff;

      } else if (i == 0) {
        // first presence is not present...assume that the first presence is the beginning of the day
        Instant previous = Instant.ofEpochSecond((Integer) _presences.get(i).get("created_date"));
        previous = previous.atZone(ZoneId.of("Z")).toLocalDate().atStartOfDay(ZoneId.of("Z")).toInstant();
        diff = (int) ChronoUnit.SECONDS.between(previous, Instant.ofEpochSecond((Integer) _presences.get(i).get("created_date")));

        totalDuration += diff;
      }
    }

    return totalDuration;
  }

  private int computeStartTime(Map<String, Map> performance) {
    checkNotNull(performance);

    int startTime = 0;

    String[] keys = performance.keySet().toArray(new String[performance.keySet().size()]);

    if (!performance.isEmpty() && (Boolean) performance.get(keys[0]).get("is_present")) {
      Instant createdDate = Instant.ofEpochSecond((Integer) performance.get(keys[0]).get("created_date"));
      Instant startOfDay = createdDate.atZone(ZoneId.of("Z")).toLocalDate().atStartOfDay(ZoneId.of("Z")).toInstant();
      startTime = (int) ChronoUnit.SECONDS.between(startOfDay, createdDate);
    }

    return startTime;
  }

  private int computeEndTime(Map<String, Map> performance) {
    checkNotNull(performance);

    int endTime = 0;

    String[] keys = performance.keySet().toArray(new String[performance.keySet().size()]);

    if (!performance.isEmpty()) {
      Instant endDate;
      if ((Boolean) performance.get(keys[keys.length - 1]).get("is_present")) {
        endDate = Instant.ofEpochSecond((Integer) performance.get(keys[keys.length - 1]).get("created_date")).atZone(ZoneId.of("Z")).toLocalDate().atTime(23, 59, 59).atZone(ZoneId.of("Z")).toInstant();
        Instant startOfDay = endDate.atZone(ZoneId.of("Z")).toLocalDate().atStartOfDay(ZoneId.of("Z")).toInstant();
        endTime = (int) ChronoUnit.SECONDS.between(startOfDay, endDate);
      } else {
        endDate = Instant.ofEpochSecond((Integer) performance.get(keys[keys.length - 1]).get("created_date"));
        Instant startOfDay = endDate.atZone(ZoneId.of("Z")).toLocalDate().atStartOfDay(ZoneId.of("Z")).toInstant();
        endTime = (int) ChronoUnit.SECONDS.between(startOfDay, endDate);
      }
    }

    return endTime;
  }
}
