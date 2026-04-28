package helen.com.produtoservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProdutoCreateDTO(
        @NotBlank
        String nome,

        @NotNull
        @Positive
        BigDecimal valor
) {
}
