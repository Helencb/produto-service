package helen.com.produtoservice.messaging.outbox;

import helen.com.produtoservice.messaging.producer.ProdutoProducer;
import helen.com.produtoservice.model.OutboxEvent;
import helen.com.produtoservice.model.OutboxStatus;
import helen.com.produtoservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private ProdutoProducer producer;

    private OutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new OutboxPublisher(repository, producer);
        ReflectionTestUtils.setField(publisher, "retryDelayMs", 30000L);
        ReflectionTestUtils.setField(publisher, "maxTentativas", 3);
    }

    private OutboxEvent eventoPendente() {
        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .exchange("produto.exchange")
                .routingKey("produto.criado")
                .eventType("helen.com.produtoservice.messaging.event.ProdutoCriadoEvent")
                .payload("{}")
                .status(OutboxStatus.PENDENTE)
                .tentativas(0)
                .correlationId("cid")
                .criadoEm(LocalDateTime.now())
                .proximaTentativaEm(LocalDateTime.now())
                .build();
    }

    @Test
    void devePublicarEMarcarComoPublicadoQuandoEnvioTemSucesso() {
        OutboxEvent evento = eventoPendente();
        when(repository.findById(evento.getId())).thenReturn(Optional.of(evento));

        publisher.tentarPublicar(evento.getId());

        verify(producer).publicarEvento(evento.getExchange(), evento.getRoutingKey(),
                evento.getEventType(), evento.getPayload(), evento.getCorrelationId());

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());
        assertEquals(OutboxStatus.PUBLICADO, captor.getValue().getStatus());
    }

    @Test
    void deveManterPendenteEAgendarNovaTentativaQuandoEnvioFalha() {
        OutboxEvent evento = eventoPendente();
        when(repository.findById(evento.getId())).thenReturn(Optional.of(evento));
        doThrow(new RuntimeException("broker indisponivel"))
                .when(producer).publicarEvento(anyString(), anyString(), anyString(), anyString(), any());

        publisher.tentarPublicar(evento.getId());

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());
        assertEquals(OutboxStatus.PENDENTE, captor.getValue().getStatus());
        assertEquals(1, captor.getValue().getTentativas());
    }

    @Test
    void deveMarcarComoFalhouAposEsgotarTentativas() {
        OutboxEvent evento = eventoPendente();
        evento.setTentativas(2);
        when(repository.findById(evento.getId())).thenReturn(Optional.of(evento));
        doThrow(new RuntimeException("broker indisponivel"))
                .when(producer).publicarEvento(anyString(), anyString(), anyString(), anyString(), any());

        publisher.tentarPublicar(evento.getId());

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());
        assertEquals(OutboxStatus.FALHOU, captor.getValue().getStatus());
        assertEquals(3, captor.getValue().getTentativas());
    }
}
