package helen.com.produtoservice.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProdutoSemEstoqueEvent(
        UUID id,
        LocalDateTime dataEvento
) {
}
