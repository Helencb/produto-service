package helen.com.produtoservice.exception;

import java.util.UUID;

public class ProdutoNotFoundException extends RuntimeException {
    public ProdutoNotFoundException(UUID id) {
        super("Produto não encontrado com id: " + id);
    }
}
