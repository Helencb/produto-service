package helen.com.produtoservice.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProdutoProducer {
    private final RabbitTemplate rabbit;

    private static final String CORRELATION_HEADER = "X-Correlation-ID";
    private static final String TYPE_ID_HEADER = "__TypeId__";

    public void publicarEvento(String exchange, String routingKey, String eventType, String payloadJson, String correlationId) {
        Message message = MessageBuilder
                .withBody(payloadJson.getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader(TYPE_ID_HEADER, eventType)
                .setHeader(CORRELATION_HEADER, correlationId)
                .build();

        rabbit.send(exchange, routingKey, message);

        log.info("[RABBITMQ] Evento publicado | correlationId={} | exchange={} | routingKey={}",
                correlationId, exchange, routingKey);
    }
}
