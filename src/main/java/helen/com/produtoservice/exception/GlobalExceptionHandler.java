package helen.com.produtoservice.exception;

import helen.com.produtoservice.dto.ApiResponse;
import helen.com.produtoservice.metrics.ErrorMetrics;
import helen.com.produtoservice.util.LogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorMetrics metrics;

    @ExceptionHandler(ProdutoNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProdutoNotFound(ProdutoNotFoundException ex) {
        metrics.increment("NOT_FOUND");

        log.error("[{}] Produto não encontrado: {}",
                LogUtil.get(),
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        metrics.increment("VALIDATION");

        Map<String, String> erros = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(e -> erros.put(e.getField(), e.getDefaultMessage()));

        log.warn("[{}] Erro de validação: {}",
                LogUtil.get(),
                erros);

        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(
                        false,
                        "Erro de validação",
                        erros
                ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoResourceFoundException ex) {
        metrics.increment("NOT_FOUND");

        log.warn("[{}] Recurso não encontrado: {}",
                LogUtil.get(),
                ex.getResourcePath()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                        false,
                        "Recurso não encontrado",
                        null));
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        metrics.increment("INTERNAL_ERROR");

        log.error("[{}] Erro inesperado",
                LogUtil.get(),
                ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                        false,
                        "Erro interno do servidor",
                        null
                ));
    }
}
