package helen.com.produtoservice.messaging.producer;

import helen.com.produtoservice.config.RabbitConfig;
import helen.com.produtoservice.messaging.event.ProdutoAtualizadoEvent;
import helen.com.produtoservice.messaging.event.ProdutoCriadoEvent;
import helen.com.produtoservice.messaging.event.ProdutoDesativadoEvent;
import helen.com.produtoservice.messaging.routing.RoutingKeys;

import helen.com.produtoservice.util.LogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProdutoProducer {
    private final RabbitTemplate rabbit;

    private static final String HEADER = "X-Correlation-ID";

    public void enviarProdutoCriado(ProdutoCriadoEvent event) {
        String correlationId = LogUtil.get();

        rabbit.convertAndSend(
                RabbitConfig.EXCHANGE,
                RoutingKeys.PRODUTO_CRIADO,
                event,
                message -> {
                    message.getMessageProperties()
                            .setHeader(HEADER, correlationId);
                    return message;
                }
        );
        log.info("[RABBITMQ] Evento ProdutoCriado enviado | correlationId={} | id={}",
                correlationId,
                event.id());
    }

    public void enviarProdutoAtualizado(ProdutoAtualizadoEvent event) {
        String correlationId = LogUtil.get();

        rabbit.convertAndSend(
                RabbitConfig.EXCHANGE,
                RoutingKeys.PRODUTO_ATUALIZADO,
                event,
                message -> {
                    message.getMessageProperties()
                            .setHeader(HEADER, correlationId);
                    return message;
                }
        );
        log.info("[RABBITMQ] Evento ProdutoAtualizado enviado | correlationId={} | id={}",
                correlationId,
                event.id());    }

    public void enviarProdutoDesativado(ProdutoDesativadoEvent event) {
        String correlationId = LogUtil.get();

        rabbit.convertAndSend(
                RabbitConfig.EXCHANGE,
                RoutingKeys.PRODUTO_DESATIVADO,
                event,
                message -> {
                    message.getMessageProperties()
                            .setHeader(HEADER, correlationId);
                    return message;
                }
        );
        log.info("[RABBITMQ] Evento ProdutoDesativado enviado | correlationId={} | id={}",
                correlationId,
                event.id());    }
}
