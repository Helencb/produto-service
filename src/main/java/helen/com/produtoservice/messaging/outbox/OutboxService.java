package helen.com.produtoservice.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import helen.com.produtoservice.model.OutboxEvent;
import helen.com.produtoservice.model.OutboxStatus;
import helen.com.produtoservice.repository.OutboxEventRepository;
import helen.com.produtoservice.util.LogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    private final OutboxPublisher publisher;

    public void registrar(String exchange, String routingKey, UUID aggregateId, Object evento) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(evento);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar evento para a outbox", e);
        }

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(aggregateId)
                .exchange(exchange)
                .routingKey(routingKey)
                .eventType(evento.getClass().getName())
                .payload(payload)
                .status(OutboxStatus.PENDENTE)
                .tentativas(0)
                .correlationId(LogUtil.get())
                .criadoEm(LocalDateTime.now())
                .proximaTentativaEm(LocalDateTime.now())
                .build();

        OutboxEvent salvo = repository.save(outboxEvent);

        log.info("[OUTBOX] Evento registrado | correlationId={} | id={} | routingKey={}",
                LogUtil.get(), salvo.getId(), routingKey);

        // Publica assim que a transação do agregado for confirmada; se falhar aqui
        // (broker fora do ar, etc.), o evento fica PENDENTE e o OutboxScheduler reconcilia depois.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publisher.tentarPublicar(salvo.getId());
                }
            });
        } else {
            publisher.tentarPublicar(salvo.getId());
        }
    }
}
