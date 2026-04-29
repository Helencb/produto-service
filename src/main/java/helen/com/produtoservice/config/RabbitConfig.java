package helen.com.produtoservice.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import static helen.com.produtoservice.messaging.routing.RoutingKeys.*;

@Configuration
public class RabbitConfig {
    //EXCHANGES
    public static final String EXCHANGE = "produto.exchange";
    public static final String DLQ_EXCHANGE = "produto.dlq.exchange";

    //QUEUES
    public static final String PRODUTO_CRIACAO_QUEUE = "produto.criacao.queue";
    public static final String PRODUTO_CATALOGO_QUEUE = "produto.catalogo.queue";
    public static final String PRODUTO_CATALOGO_DLQ_QUEUE = "produto.catalogo.dlq.queue";
    public static final String DLQ_QUEUE = "produto.dlq.queue";


    //EXCHANGES
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public TopicExchange dlqExchange() {
        return new TopicExchange(DLQ_EXCHANGE);
    }

   // QUEUES
    @Bean
    public Queue produtoCriacaoQueue() {
        return QueueBuilder
                .durable(PRODUTO_CRIACAO_QUEUE)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key",RK_DLQ)
                .build();
    }

    @Bean
    public Queue produtoCatalogoQueue() {
        return QueueBuilder
                .durable(PRODUTO_CATALOGO_QUEUE)
                .withArgument(
                        "x-dead-letter-exchange",
                        DLQ_EXCHANGE)
                .withArgument(
                        "x-dead-letter-routing-key",
                        PRODUTO_CATALOGO_DLQ)
                .build();
    }

    @Bean
    public Queue produtoCatalogoDlqQueue() {
        return QueueBuilder
                .durable(PRODUTO_CATALOGO_DLQ_QUEUE)
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
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(dlqQueue())
                .to(dlqExchange())
                .with(RK_DLQ);
    }

    @Bean
    public Binding produtoCatalogoDlqBinding() {
        return BindingBuilder
                .bind(produtoCatalogoDlqQueue())
                .to(dlqExchange())
                .with(PRODUTO_CATALOGO_DLQ);
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
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {

        return RetryInterceptorBuilder
                .stateless()
                .maxAttempts(3)
                .recoverer(
                        new RejectAndDontRequeueRecoverer()
                )
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            RetryOperationsInterceptor retryInterceptor,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(retryInterceptor);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);

        return factory;
    }
}
