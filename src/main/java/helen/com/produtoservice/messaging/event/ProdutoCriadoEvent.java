package helen.com.produtoservice.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProdutoCriadoEvent(
        UUID id,
        String nome,
        BigDecimal valor,
        LocalDateTime dataCriacao
) {
}
