package helen.com.produtoservice.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import helen.com.produtoservice.messaging.event.ProdutoCriadoEvent;
import helen.com.produtoservice.model.OutboxEvent;
import helen.com.produtoservice.model.OutboxStatus;
import helen.com.produtoservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private OutboxPublisher publisher;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void deveRegistrarEventoPendenteETentarPublicarImediatamente() {
        OutboxService service = new OutboxService(repository, objectMapper, publisher);

        UUID produtoId = UUID.randomUUID();
        ProdutoCriadoEvent event = new ProdutoCriadoEvent(produtoId, "Rosa", new BigDecimal("10.00"), LocalDateTime.now());

        when(repository.save(any(OutboxEvent.class))).thenAnswer(invocation -> {
            OutboxEvent e = invocation.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        service.registrar("produto.exchange", "produto.criado", produtoId, event);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());

        OutboxEvent salvo = captor.getValue();
        assertEquals(produtoId, salvo.getAggregateId());
        assertEquals("produto.exchange", salvo.getExchange());
        assertEquals("produto.criado", salvo.getRoutingKey());
        assertEquals(OutboxStatus.PENDENTE, salvo.getStatus());
        assertEquals(ProdutoCriadoEvent.class.getName(), salvo.getEventType());
        assertTrue(salvo.getPayload().contains("Rosa"));

        // fora de uma transação, a publicação é tentada de forma síncrona
        verify(publisher).tentarPublicar(salvo.getId());
    }
}
