package helen.com.produtoservice.integration;

import helen.com.produtoservice.config.RabbitSimpleTestConfig;
import helen.com.produtoservice.consumer.TesteQueueListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("rabbit-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(RabbitSimpleTestConfig.class)
public class RabbitIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TesteQueueListener listener;

    @Test
    void deveEnviarEMensagemParaFila() {
        rabbitTemplate.convertAndSend("teste.queue", "mensagem teste");
        assertTrue(true);
    }

    @Test
    void deveConsumirMensagemDaFila() throws InterruptedException {
        String msg = "hello teste";

        rabbitTemplate.convertAndSend("teste.queue", msg);

        boolean recebeu = listener.getLatch().await(5, TimeUnit.SECONDS);

        assertTrue(recebeu);
        assertEquals(msg, listener.getMensagem());
    }
}
