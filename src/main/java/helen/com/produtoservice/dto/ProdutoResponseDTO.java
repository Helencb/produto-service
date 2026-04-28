package helen.com.produtoservice.dto;

import helen.com.produtoservice.model.StatusProduto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoResponseDTO(
        UUID id,
        String nome,
        BigDecimal valor,
        StatusProduto status,
        boolean ativo
) {
}
