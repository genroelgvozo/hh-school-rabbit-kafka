package ru.hh.boksh.messaging.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitListener.class);
  private final Connection connection;
  private final Executor channelCloser = Executors.newSingleThreadExecutor();
  private final Executor futureExecutor = Executors.newCachedThreadPool();

  public RabbitPublisher(ConnectionFactory connectionFactory) {
    try {
      connection = connectionFactory.newConnection();
    } catch (TimeoutException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public CompletableFuture<Void> send(String exchange, String routingKey, byte[] body) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    try {
      Channel channel = connection.createChannel();
      if (channel == null) {
        future.completeExceptionally(new IllegalStateException("fail to get channel"));
        return future;
      }

      channel.confirmSelect();
      ConfirmCallback onAckCallback = (sequenceNumber, multiple) -> {
        closeChannel(channel);
        futureExecutor.execute(() -> future.complete(null));
      };
      ConfirmCallback onNackCallback = (sequenceNumber, multiple) -> {
        closeChannel(channel);
        futureExecutor.execute(() -> future.completeExceptionally(new RuntimeException(String.format("failed to get ack for %s", sequenceNumber))));
      };
      channel.addConfirmListener(onAckCallback, onNackCallback);

      channel.basicPublish(exchange, routingKey, null, body);
      return future;
    } catch (IOException | RuntimeException e) {
      future.completeExceptionally(e);
      return future;
    }
  }

  private void closeChannel(Channel channel) {
    channelCloser.execute(() -> {
      try {
        channel.close();
      } catch (IOException | TimeoutException e) {
        LOGGER.error("exception during channel close", e);
      }
    });
  }
}
