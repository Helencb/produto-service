package helen.com.produtoservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoCreateDTO(
        @NotBlank(message = "O nome do produto é obrigatório")
        @Size(min = 3, max = 100,
                message = "O nome deve ter entre 3 e 100 caracteres")
        String nome,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(min = 10, max = 500,
                message = "A descrição deve ter entre 10 e 500 caracteres")
        String descricao,

        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal valor

) {
}
