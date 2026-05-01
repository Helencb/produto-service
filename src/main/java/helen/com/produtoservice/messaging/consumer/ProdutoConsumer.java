package helen.com.produtoservice.messaging.consumer;


import helen.com.produtoservice.config.RabbitConfig;
import helen.com.produtoservice.messaging.event.ProdutoEmEstoqueEvent;
import helen.com.produtoservice.messaging.event.ProdutoSemEstoqueEvent;
import helen.com.produtoservice.model.StatusProduto;
import helen.com.produtoservice.service.ProdutoService;
import helen.com.produtoservice.util.LogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
@RabbitListener(queues = RabbitConfig.PRODUTO_CATALOGO_QUEUE)
public class ProdutoConsumer {

    private final ProdutoService service;
    private static final String HEADER = "X-Correlation-ID";

    @RabbitHandler
    public void consumirSemEstoque(ProdutoSemEstoqueEvent event, Message message) {

        String correlationId =  LogUtil.getOrCreate(
                (String) message.getMessageProperties()
                .getHeaders()
                .get(HEADER));

        LogUtil.set(correlationId);

        try {
            log.info("[RABBITMQ] Evento ProdutoSemEstoque recebido | correlationId={}",
                    LogUtil.get());

            service.atualizarStatus(event.id(), StatusProduto.SEM_ESTOQUE);

            log.info("[RABBITMQ] Produto atualizado para SEM_ESTOQUE | correlationId={} | id={}",
                    correlationId,
                    event.id());

        } finally {
            LogUtil.clear();
        }
    }

    @RabbitHandler
    public void consumirEmEstoque(ProdutoEmEstoqueEvent event, Message message) {

        String correlationId = LogUtil.getOrCreate(
                (String) message.getMessageProperties()
                .getHeaders()
                .get(HEADER));

       LogUtil.set(correlationId);

        try {
            log.info("[RABBITMQ] Evento ProdutoEmEstoque recebido | correlationId={} | id={}",
                    LogUtil.get(),
                    event.id());

            service.atualizarStatus(event.id(), StatusProduto.EM_ESTOQUE);

            log.info("[RABBITMQ] Produto atualizado para EM_ESTOQUE | correlationId={} | id={}",
                    LogUtil.get(),
                    event.id());

        } finally {
            LogUtil.clear();
        }
    }
}
