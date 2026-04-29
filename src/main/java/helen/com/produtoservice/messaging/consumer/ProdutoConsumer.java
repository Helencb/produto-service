package helen.com.produtoservice.messaging.consumer;


import helen.com.produtoservice.config.RabbitConfig;
import helen.com.produtoservice.messaging.event.ProdutoEmEstoqueEvent;
import helen.com.produtoservice.messaging.event.ProdutoSemEstoqueEvent;
import helen.com.produtoservice.model.StatusProduto;
import helen.com.produtoservice.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
@RabbitListener(queues = RabbitConfig.PRODUTO_CATALOGO_QUEUE)
public class ProdutoConsumer {

    private final ProdutoService service;

    @RabbitHandler
    public void consumirSemEstoque(ProdutoSemEstoqueEvent event) {

        log.info("[RABBITMQ] Produto sem estoque recebido: {}", event.id());

        service.atualizarStatus(event.id(), StatusProduto.SEM_ESTOQUE);

        log.info("[RABBITMQ] Produto {} atualizado para SEM_ESTOQUE", event.id());

    }

    @RabbitHandler
    public void consumirEmEstoque(ProdutoEmEstoqueEvent event) {

        log.info("[RABBITMQ] Produto em estoque recebido: {}", event.id());

        service.atualizarStatus(event.id(), StatusProduto.EM_ESTOQUE);

        log.info("[RABBITMQ] Produto {} atualizado para EM_ESTOQUE", event.id());
    }
}
