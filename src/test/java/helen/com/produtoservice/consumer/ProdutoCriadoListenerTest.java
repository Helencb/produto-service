package helen.com.produtoservice.consumer;

import helen.com.produtoservice.messaging.event.ProdutoCriadoEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Profile("produto-test")
public class ProdutoCriadoListenerTest {

    private CountDownLatch latch = new CountDownLatch(1);
    private ProdutoCriadoEvent evento;

    @RabbitListener(queues = "produto.criado.integration")
    public void consumir(ProdutoCriadoEvent event) {
        this.evento = event;
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public ProdutoCriadoEvent getEvento() {
        return evento;
    }
}

