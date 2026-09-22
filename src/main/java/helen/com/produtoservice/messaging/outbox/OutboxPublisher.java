package helen.com.produtoservice.messaging.outbox;

import helen.com.produtoservice.messaging.producer.ProdutoProducer;
import helen.com.produtoservice.model.OutboxEvent;
import helen.com.produtoservice.model.OutboxStatus;
import helen.com.produtoservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository repository;
    private final ProdutoProducer producer;

    @Value("${app.outbox.retry-delay-ms}")
    private long retryDelayMs;

    @Value("${app.outbox.max-tentativas:5}")
    private int maxTentativas;

    @Transactional
    public void tentarPublicar(UUID outboxEventId) {
        repository.findById(outboxEventId).ifPresent(this::publicar);
    }

    private void publicar(OutboxEvent evento) {
        if (evento.getStatus() == OutboxStatus.PUBLICADO) {
            return;
        }

        try {
            producer.publicarEvento(
                    evento.getExchange(),
                    evento.getRoutingKey(),
                    evento.getEventType(),
                    evento.getPayload(),
                    evento.getCorrelationId()
            );

            evento.setStatus(OutboxStatus.PUBLICADO);
            evento.setPublicadoEm(LocalDateTime.now());
            repository.save(evento);

        } catch (Exception e) {
            int tentativas = evento.getTentativas() + 1;

            log.error("[OUTBOX] Falha ao publicar evento | correlationId={} | id={} | tentativa={}",
                    evento.getCorrelationId(), evento.getId(), tentativas, e);

            evento.setTentativas(tentativas);
            evento.setProximaTentativaEm(LocalDateTime.now().plus(Duration.ofMillis(retryDelayMs)));
            evento.setStatus(tentativas >= maxTentativas ? OutboxStatus.FALHOU : OutboxStatus.PENDENTE);
            repository.save(evento);
        }
    }
}
