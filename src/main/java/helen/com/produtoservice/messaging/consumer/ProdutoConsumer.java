package helen.com.produtoservice.messaging.consumer;


import helen.com.produtoservice.messaging.event.ProdutoEmEstoqueEvent;
import helen.com.produtoservice.messaging.event.ProdutoSemEstoqueEvent;
import helen.com.produtoservice.model.StatusProduto;
import helen.com.produtoservice.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProdutoConsumer {

    private final ProdutoService service;

    @RabbitListener(queues = "produto.queue")
    public void consumirSemEstoque(ProdutoSemEstoqueEvent event) {
        log.info("Produto sem estoque recebido: {}", event.id());

        service.atualizarStatus(event.id(), StatusProduto.SEM_ESTOQUE);
    }

    @RabbitListener(queues = "produto.queue")
    public void consumirEmEstoque(ProdutoEmEstoqueEvent event) {

        log.info("Produto em estoque recebido: {}", event.id());

        service.atualizarStatus(event.id(), StatusProduto.EM_ESTOQUE);
    }
}
