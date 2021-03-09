/**
 * MessageSchema spec: @see https://wiki.mm.meshkorea.net/display/MES/Message+Schema
 */
package dev.appkr.espoc.stream;

import dev.appkr.espoc.config.Constants.MessageKey;
import dev.appkr.espoc.config.Constants.MessagePolicy;
import dev.appkr.espoc.domain.PersistentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static dev.appkr.espoc.config.Constants.PROJECT_NAME;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageProducer {

  private final MessageChannel messageChannel;

  public boolean produce(PersistentEvent persistentEvent) {
    final String body = persistentEvent.getBody();
    Message<?> message = MessageBuilder
        .withPayload(body)
        .setHeader(MessageKey.ID, persistentEvent.getEventId())
        .setHeader(MessageKey.TYPE, persistentEvent.getEventType())
        .setHeader(MessageKey.VERSION, 1)
        .setHeader(MessageKey.SOURCE, PROJECT_NAME)
        .setHeader(MessageKey.RESOURCE, body.getClass().getSimpleName())
        .setHeader(MessageKey.PARTITION_KEY, persistentEvent.getPartitionKey())
        .build();

    return messageChannel.send(message, MessagePolicy.DEFAULT_TIMEOUT);
  }
}
