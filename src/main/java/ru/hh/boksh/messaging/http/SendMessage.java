package ru.hh.boksh.messaging.http;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.boksh.messaging.kafka.NaiveKafkaPublisher;
import ru.hh.boksh.messaging.rabbit.RabbitPublisher;
import ru.hh.boksh.messaging.utils.Utils;

@RestController
public class SendMessage {

  private static final Logger LOGGER = LoggerFactory.getLogger(SendMessage.class);

  private final RabbitPublisher rabbitPublisher;
  private final NaiveKafkaPublisher naiveKafkaPublisher;

  public SendMessage(RabbitPublisher rabbitPublisher, NaiveKafkaPublisher naiveKafkaPublisher) {
    this.rabbitPublisher = rabbitPublisher;
    this.naiveKafkaPublisher = naiveKafkaPublisher;
  }

  @RequestMapping(value = "/rabbit/exchange/{exchange}/routing_key/{routingKey}", method = RequestMethod.POST)
  public void sendMessageToRabbit(
      @PathVariable("exchange") String exchange,
      @PathVariable("routingKey") String routingKey,
      @RequestParam(value = "messageBody", required = false) String messageBody,
      @RequestParam(value = "blocking", defaultValue = "false") boolean blocking
  ) throws ExecutionException, InterruptedException {
    LOGGER.info("got rabbit message to send: exchange={}, routing_key={}", exchange, routingKey);
    if (messageBody == null) {
      messageBody = Utils.getNowString();
    }
    CompletableFuture<Void> sendFuture = rabbitPublisher.send(exchange, routingKey, messageBody.getBytes(StandardCharsets.UTF_8));
    LOGGER.info("send rabbit message: exchange={}, routing_key={}", exchange, routingKey);

    sendFuture = sendFuture.thenAccept((ignored) -> LOGGER.info("got ack for message: exchange={}, routing_key={}", exchange, routingKey));
    if (blocking) {
      sendFuture.get();
    }
  }

  @RequestMapping(value = "/kafka/topic/{topic}", method = RequestMethod.POST)
  public void sendMessageToKafka(
      @PathVariable("topic") String topic,
      @RequestParam("key") String key,
      @RequestParam(value = "messageBody", required = false) String messageBody,
      @RequestParam(value = "slow", defaultValue = "true") boolean slow,
      @RequestParam(value = "blocking", defaultValue = "false") boolean blocking
  ) throws ExecutionException, InterruptedException {
    LOGGER.info("got kafka message to send: topic={}, key={}", topic, key);
    if (messageBody == null) {
      messageBody = Utils.getNowString();
    }

    CompletableFuture<RecordMetadata> sendFuture = naiveKafkaPublisher.send(topic, key, messageBody, slow);
    LOGGER.info("send kafka message: topic={}, key={}", topic, key);

    sendFuture = sendFuture.thenApply((recordMetadata) -> {
      LOGGER.info("got ack for kafka message: topic={}, key={}", topic, key);
      return recordMetadata;
    });

    if (blocking) {
      sendFuture.get();
    }

  }
}
