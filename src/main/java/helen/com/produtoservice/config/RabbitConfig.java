package helen.com.produtoservice.config;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

import static helen.com.produtoservice.messaging.routing.RoutingKeys.*;

@Configuration
public class RabbitConfig {
    //EXCHANGES
    public static final String EXCHANGE = "produto.exchange";
    public static final String RETRY_EXCHANGE = "produto.retry.exchange";
    public static final String DLQ_EXCHANGE = "produto.dlq.exchange";

    //QUEUES
    public static final String PRODUTO_CRIACAO_QUEUE = "produto.criacao.queue";
    public static final String PRODUTO_CATALOGO_QUEUE = "produto.catalogo.queue";
    public static final String RETRY_QUEUE = "produto.retry.queue";
    public static final String DLQ_QUEUE = "produto.dlq.queue";


    //EXCHANGES
    @Bean // EXCHANGE PRINCIPAL "produto.exchange"
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public TopicExchange retryExchange() {
        return new TopicExchange(RETRY_EXCHANGE);
    }

    @Bean
    public TopicExchange dlqExchange() {
        return new TopicExchange(DLQ_EXCHANGE);
    }

   // QUEUES
    @Bean  // QUEUE PRINCIPAL "produto.queue"
    public Queue produtoCriacaoQueue() {
        return QueueBuilder
                .durable(PRODUTO_CRIACAO_QUEUE)
                .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key",RK_RETRY)
                .build();
    }

    @Bean
    public Queue produtoCatalogoQueue() {

        return QueueBuilder
                .durable(PRODUTO_CATALOGO_QUEUE)

                .withArgument(
                        "x-dead-letter-exchange",
                        RETRY_EXCHANGE
                )

                .withArgument(
                        "x-dead-letter-routing-key",
                        RK_RETRY
                )

                .build();
    }

    @Bean
    public Queue retryQueue() {
        return QueueBuilder.
                durable(RETRY_QUEUE)
                .withArgument("x-message-ttl", 5000)
                .withArgument(
                        "x-dead-letter-exchange",
                        EXCHANGE)
                .withArgument("x-dead-letter-routing-key", PRODUTO_CRIADO)
                .build();
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder
                .durable(DLQ_QUEUE)
                .build();
    }

    //BINDINGS
    @Bean
    public Binding produtoCriadoBinding() {
        return BindingBuilder
                .bind(produtoCriacaoQueue())
                .to(exchange())
                .with(PRODUTO_CRIADO);
    }

    @Bean
    public Binding retryBinding() {
        return BindingBuilder
                .bind(retryQueue())
                .to(retryExchange())
                .with(RK_RETRY);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(dlqQueue())
                .to(dlqExchange())
                .with(RK_DLQ);
    }

    //ESTOQUE
    @Bean
    public Binding bindingProdutoSemEstoque() {
        return BindingBuilder
                .bind(produtoCatalogoQueue())
                .to(exchange())
                .with(PRODUTO_SEM_ESTOQUE);
    }

    @Bean
    public Binding bindingProdutoEmEstoque() {
        return BindingBuilder
                .bind(produtoCatalogoQueue())
                .to(exchange())
                .with(PRODUTO_EM_ESTOQUE);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
