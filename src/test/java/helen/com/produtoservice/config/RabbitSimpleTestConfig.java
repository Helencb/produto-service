package helen.com.produtoservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RabbitSimpleTestConfig {
    @Bean
    public Queue testQueue() {
        return new Queue("teste.queue", true);
    }
}
