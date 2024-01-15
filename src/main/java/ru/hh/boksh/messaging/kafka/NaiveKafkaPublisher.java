package ru.hh.boksh.messaging.kafka;

import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Qualifier;

public class NaiveKafkaPublisher {

  private final Producer<String, String> defaultKafkaProducer;
  private final Producer<String, String> slowKafkaProducer;

  public NaiveKafkaPublisher(
      @Qualifier("DefaultProducer") Producer<String, String> defaultKafkaProducer,
      @Qualifier("SlowProducer") Producer<String, String> slowKafkaProducer
  ) {
    this.defaultKafkaProducer = defaultKafkaProducer;
    this.slowKafkaProducer = slowKafkaProducer;
  }

  public CompletableFuture<RecordMetadata> send(String topic, String key, String data, boolean slow) {
    CompletableFuture<RecordMetadata> future = new CompletableFuture<>();
    var kafkaProducer = slow ? slowKafkaProducer : defaultKafkaProducer;
    try {
      kafkaProducer.send(new ProducerRecord<>(topic, key, data), (metadata, exception) -> {
        if (exception != null) {
          future.completeExceptionally(exception);
        }
        future.complete(metadata);
      });
    } catch (RuntimeException ex) {
      future.completeExceptionally(ex);
    }

    return future;
  }

}
