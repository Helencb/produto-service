package helen.com.produtoservice.integration;

import helen.com.produtoservice.config.RabbitTestConfig;
import helen.com.produtoservice.consumer.ProdutoCriadoListenerTest;
import helen.com.produtoservice.dto.ProdutoCreateDTO;
import helen.com.produtoservice.dto.ProdutoResponseDTO;
import helen.com.produtoservice.messaging.event.ProdutoCriadoEvent;
import helen.com.produtoservice.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test", "produto-test"})
@Import(RabbitTestConfig.class)
public class ProdutoServiceIntegrationTest {
    @Autowired
    private ProdutoService service;

    @Autowired
    private ProdutoCriadoListenerTest listener;

    @Test
    void deveCriarProdutoEPublicarEvento() throws InterruptedException {
        ProdutoCreateDTO dto = new ProdutoCreateDTO("Notebook", "Um computador horrivel", BigDecimal.valueOf(1234.88));

        ProdutoResponseDTO response = service.criar(dto);
        boolean recebeu = listener.getLatch().await(5, TimeUnit.SECONDS);
        assertTrue(recebeu);

        ProdutoCriadoEvent event = listener.getEvento();

        assertNotNull(event);
        assertEquals(response.id(), event.id());
        assertEquals("Notebook", event.nome());
    }
}
