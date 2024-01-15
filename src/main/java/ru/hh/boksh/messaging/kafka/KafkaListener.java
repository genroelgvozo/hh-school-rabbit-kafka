package ru.hh.boksh.messaging.kafka;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.boksh.messaging.utils.Utils;

public class KafkaListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListener.class);
  private static final Duration POLL_TIMEOUT = Duration.ofSeconds(3);
  private final NaiveKafkaConsumerFactory naiveKafkaConsumerFactory;
  private final Executor executor = Executors.newCachedThreadPool();

  public KafkaListener(NaiveKafkaConsumerFactory naiveKafkaConsumerFactory) {
    this.naiveKafkaConsumerFactory = naiveKafkaConsumerFactory;
  }

  @PostConstruct
  public void startListen() {
    listenToTopic("example_topic", "example_app__group1", true);
    listenToTopicManual("example_topic");
  }

  private void listenToTopic(String topicName, String consumerGroup, boolean commitOffsetToKafka) {
    executor.execute(() -> {
      try (Consumer<String, String> kafkaConsumer = naiveKafkaConsumerFactory.createKafkaConsumer(consumerGroup)) {
        kafkaConsumer.subscribe(List.of(topicName));
        while (!Thread.currentThread().isInterrupted()) {
          ConsumerRecords<String, String> consumedRecords = kafkaConsumer.poll(POLL_TIMEOUT);
          if (consumedRecords.isEmpty()) {
            continue;
          }

          consumedRecords.forEach(record ->
              Utils.getLatencyMillis(record.value()).ifPresentOrElse(
                  (latencyMillis) -> LOGGER.info("Kafka: got record for consumer group {}, latency {}: {}", consumerGroup, latencyMillis, record),
                  () -> LOGGER.info("Kafka: got record for consumer group {}: {}. Latency can't be calculated", consumerGroup, record)
              )
          );

          if (commitOffsetToKafka) {
            kafkaConsumer.commitSync();
          }
        }
      }
    });
  }

  private void listenToTopicManual(String topicName) {
    executor.execute(() -> {
      try (Consumer<String, String> kafkaConsumer = naiveKafkaConsumerFactory.createKafkaConsumer(null)) {

        List<TopicPartition> allPartitions = kafkaConsumer.partitionsFor(topicName).stream().map(p ->
            new TopicPartition(
                p.topic(),
                p.partition()
            )
        ).toList();

        kafkaConsumer.assign(allPartitions);
        kafkaConsumer.seekToEnd(allPartitions);

        while (!Thread.currentThread().isInterrupted()) {
          ConsumerRecords<String, String> consumedRecords = kafkaConsumer.poll(POLL_TIMEOUT);
          if (consumedRecords.isEmpty()) {
            continue;
          }

          consumedRecords.forEach(record -> {
            Utils.getLatencyMillis(record.value()).ifPresentOrElse(
                (latencyMillis) -> LOGGER.info("Kafka: got record for consumer group {}, latency {}: {}", "MISSING", latencyMillis, record),
                () -> LOGGER.info("Kafka: got record for consumer group {}: {}. Latency can't be calculated", "MISSING", record)
            );
          });
        }
      }
    });
  }
}

