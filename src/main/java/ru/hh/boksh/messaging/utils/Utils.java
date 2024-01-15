package ru.hh.boksh.messaging.utils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class Utils {

  public static Optional<Long> getLatencyMillis(String content) {
    try {
      OffsetDateTime sendTime = OffsetDateTime.parse(content, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      long latencyMillis = Duration.between(sendTime, OffsetDateTime.now()).toMillis();
      return Optional.of(latencyMillis);
    } catch (DateTimeParseException ex) {
      return Optional.empty();
    }
  }

  public static String getNowString() {
    return OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }
}
