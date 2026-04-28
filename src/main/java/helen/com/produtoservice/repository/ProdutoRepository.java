package helen.com.produtoservice.repository;

import helen.com.produtoservice.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
    Page<Produto> findAllByAtivoTrue(Pageable pageable);

    Optional<Produto> findByIdAndAtivoTrue(UUID uuid);
}
