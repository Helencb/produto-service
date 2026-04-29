package helen.com.produtoservice.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class TestListener {
    private CountDownLatch latch = new CountDownLatch(1);
    private String mensagem;

    @RabbitListener(queues = "teste.queue")
    public void receive(String msg) {
        this.mensagem = msg;
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public String getMessagem() {
        return mensagem;
    }
}

