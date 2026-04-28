package helen.com.produtoservice.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        boolean sucesso,
         String mensagem,
        T dados,
        LocalDateTime timestamp
) {
    public ApiResponse(boolean sucesso, String mensagem, T dados) {
        this(sucesso, mensagem, dados, LocalDateTime.now());
    }
}

