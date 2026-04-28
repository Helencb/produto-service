package helen.com.produtoservice.messaging.producer;

import helen.com.produtoservice.config.RabbitConfig;
import helen.com.produtoservice.messaging.event.ProdutoAtualizadoEvent;
import helen.com.produtoservice.messaging.event.ProdutoCriadoEvent;
import helen.com.produtoservice.messaging.event.ProdutoDesativadoEvent;
import helen.com.produtoservice.messaging.routing.RoutingKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProdutoProducer {
    private final RabbitTemplate rabbit;

    public void enviarProdutoCriado(ProdutoCriadoEvent event) {
        rabbit.convertAndSend(
                RabbitConfig.EXCHANGE,
                RoutingKeys.PRODUTO_CRIADO,
                event
        );
        log.info("[RABBITMQ] Evento produto criado enviado: {}", event);
    }

    public void enviarProdutoAtualizado(ProdutoAtualizadoEvent event) {
        rabbit.convertAndSend(
                RabbitConfig.EXCHANGE,
                RoutingKeys.PRODUTO_ATUALIZADO,
                event
        );
        log.info("[RABBITMQ] Evento produto criado enviado: {}", event);
    }

    public void enviarProdutoDesativado(ProdutoDesativadoEvent event) {
        rabbit.convertAndSend(
                RabbitConfig.EXCHANGE,
                RoutingKeys.PRODUTO_DESATIVADO,
                event
        );
        log.info("[RABBITMQ] Evento produto criado enviado: {}", event);
    }
}
