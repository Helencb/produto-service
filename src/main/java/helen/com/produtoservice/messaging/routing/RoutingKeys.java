package helen.com.produtoservice.messaging.routing;

public class RoutingKeys {
    public static final String PRODUTO_CRIADO =
            "produto.criado";

    public static final String PRODUTO_ATUALIZADO =
            "produto.atualizado";

    public static final String PRODUTO_DESATIVADO =
            "produto.desativado";

    public static final String PRODUTO_SEM_ESTOQUE =
            "produto.sem-estoque";

    public static final String PRODUTO_EM_ESTOQUE =
            "produto.em-estoque";

    public static final String RK_RETRY =
            "produto.retry";

    public static final String RK_DLQ =
            "produto.dlq";


    private RoutingKeys() {}
}
