package helen.com.produtoservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false, length = 100)
    private String exchange;

    @Column(nullable = false, length = 100)
    private String routingKey;

    @Column(nullable = false, length = 255)
    private String eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(nullable = false)
    private int tentativas;

    private String correlationId;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime proximaTentativaEm;

    private LocalDateTime publicadoEm;
}
