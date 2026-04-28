package helen.com.produtoservice.config;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "produto.exchange";
    public static final String QUEUE = "produto.queue";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding bindingProdutoSemEstoque() {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with("produto.sem-estoque");
    }

    @Bean
    public Binding bindingProdutoEmEstoque() {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with("produto.em-estoque");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
