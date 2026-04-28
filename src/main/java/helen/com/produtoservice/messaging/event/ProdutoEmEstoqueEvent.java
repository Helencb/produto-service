package helen.com.produtoservice.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProdutoEmEstoqueEvent(
        UUID id,
        LocalDateTime dataEvento
) {
}
