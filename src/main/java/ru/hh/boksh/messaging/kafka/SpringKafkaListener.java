package ru.hh.boksh.messaging.kafka;

import java.util.List;
import org.apache.kafka.clients.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import ru.hh.boksh.messaging.utils.Utils;

public class SpringKafkaListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringKafkaListener.class);
  public static final String SPRING_GROUP_1 = "spring_group_1";
  public static final String SPRING_GROUP_2 = "spring_group_2";
  public static final String SPRING_GROUP_3 = "spring_group_3";

  @KafkaListener(groupId = SPRING_GROUP_1, topics = "example_topic")
  public void listen(String value, Acknowledgment ack) {
    Utils.getLatencyMillis(value).ifPresentOrElse(
        (latencyMillis) -> LOGGER.info("SpringKafka: got record for consumer group {}, latency {}: {}", SPRING_GROUP_1, latencyMillis, value),
        () -> LOGGER.info("SpringKafka: got record for consumer group {}: {}. Latency can't be calculated", SPRING_GROUP_1, value)
    );
    ack.acknowledge();
  }

  @KafkaListener(groupId = SPRING_GROUP_2, topics = "example_topic")
  public void listenBatch(List<String> values, Acknowledgment ack) {
    values.forEach(value ->
        Utils.getLatencyMillis(value).ifPresentOrElse(
            (latencyMillis) -> LOGGER.info("SpringKafka2: got record for consumer group {}, latency {}: {}", SPRING_GROUP_2, latencyMillis, value),
            () -> LOGGER.info("SpringKafka2: got record for consumer group {}: {}. Latency can't be calculated", SPRING_GROUP_2, value)
        )
    );
    ack.acknowledge();
  }

  @KafkaListener(groupId = SPRING_GROUP_3, topics = "example_topic")
  public void listenBatch(Consumer<String, String> consumer, List<String> records, Acknowledgment ack) {
    records.forEach(value ->
        Utils.getLatencyMillis(value).ifPresentOrElse(
            (latencyMillis) -> LOGGER.info("SpringKafka3: got record for consumer group {}, latency {}: {}", SPRING_GROUP_3, latencyMillis, value),
            () -> LOGGER.info("SpringKafka3: got record for consumer group {}: {}. Latency can't be calculated", SPRING_GROUP_3, value)
        )
    );
    ack.acknowledge();
  }

}
