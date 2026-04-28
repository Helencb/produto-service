package helen.com.produtoservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nome;

    private String descricao;

    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private StatusProduto status;

    private boolean ativo;

    public Produto() {
        this.id = UUID.randomUUID();
        this.ativo = true;
        this.status = StatusProduto.CRIADO;
    }
}
