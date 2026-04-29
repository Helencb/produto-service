package helen.com.produtoservice.messaging.consumer;

import helen.com.produtoservice.config.RabbitConfig;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProdutoDlqConsumer {

    @RabbitListener(queues = RabbitConfig.PRODUTO_CATALOGO_DLQ_QUEUE)
    public void consumirDlq(Message message) {
        log.error(
                """
                MENSAGEM ENVIADA PARA DLQ
                Payload: {}
                Headers: {}
                """,
                new String(message.getBody()),
                message.getMessageProperties().getHeaders()
        );
    }
}