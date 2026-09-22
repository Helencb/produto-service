package helen.com.produtoservice.messaging.outbox;

import helen.com.produtoservice.model.OutboxEvent;
import helen.com.produtoservice.model.OutboxStatus;
import helen.com.produtoservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.outbox", name = "auto-publish", havingValue = "true", matchIfMissing = true)
public class OutboxScheduler {

    private static final int LOTE = 50;

    private final OutboxEventRepository repository;
    private final OutboxPublisher publisher;

    @Scheduled(fixedDelayString = "${app.outbox.retry-delay-ms}")
    public void reconciliar() {
        List<OutboxEvent> pendentes = repository
                .findByStatusInAndProximaTentativaEmLessThanEqualOrderByCriadoEmAsc(
                        List.of(OutboxStatus.PENDENTE),
                        LocalDateTime.now(),
                        PageRequest.of(0, LOTE)
                );

        if (pendentes.isEmpty()) {
            return;
        }

        log.info("[OUTBOX] Reconciliando {} evento(s) pendente(s)", pendentes.size());

        pendentes.forEach(evento -> publisher.tentarPublicar(evento.getId()));
    }
}
