package helen.com.produtoservice.repository;

import helen.com.produtoservice.model.OutboxEvent;
import helen.com.produtoservice.model.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, java.util.UUID> {

    List<OutboxEvent> findByStatusInAndProximaTentativaEmLessThanEqualOrderByCriadoEmAsc(
            List<OutboxStatus> status,
            LocalDateTime referencia,
            Pageable pageable
    );
}
