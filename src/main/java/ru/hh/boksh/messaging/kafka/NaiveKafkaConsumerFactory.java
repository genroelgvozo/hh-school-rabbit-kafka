package ru.hh.boksh.messaging.kafka;

import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class NaiveKafkaConsumerFactory {

  private final String servers;

  public NaiveKafkaConsumerFactory(String servers) {
    this.servers = servers;
  }

  public Consumer<String, String> createKafkaConsumer(String consumerGroup) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, "example_app_" + UUID.randomUUID().toString().toLowerCase());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    if (consumerGroup != null) {
      props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
    }
    return new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());
  }

}
