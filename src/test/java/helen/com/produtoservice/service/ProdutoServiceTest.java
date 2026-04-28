package helen.com.produtoservice.service;

import helen.com.produtoservice.dto.ProdutoCreateDTO;
import helen.com.produtoservice.dto.ProdutoResponseDTO;
import helen.com.produtoservice.mapper.ProdutoMapper;
import helen.com.produtoservice.messaging.producer.ProdutoProducer;
import helen.com.produtoservice.model.Produto;
import helen.com.produtoservice.model.StatusProduto;
import helen.com.produtoservice.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProdutoServiceTest {
    @Mock
    private ProdutoRepository repoditory;

    @Mock
    private ProdutoProducer producer;

    @Mock
    private ProdutoMapper mapper;

    @InjectMocks
    private ProdutoService service;

    @Test
    void deveCriarProduto() {
        ProdutoCreateDTO dto = new ProdutoCreateDTO("Rosa", new BigDecimal("10.00"));

        Produto produto = new Produto();
        produto.setNome("Rosa");
        produto.setValor(new BigDecimal("10.00"));

        Produto salvo = new Produto();
        salvo.setId(UUID.randomUUID());
        salvo.setNome("Rosa");
        salvo.setValor(new BigDecimal("10.00"));

        ProdutoResponseDTO responseDTO = new ProdutoResponseDTO(salvo.getId(), "Rosa", new BigDecimal("10.00"), StatusProduto.CRIADO, produto.isAtivo());

        when(mapper.toEntity(dto)).thenReturn(produto);
        when(repoditory.save(produto)).thenReturn(salvo);
        when(mapper.toResponse(salvo)).thenReturn(responseDTO);

        ProdutoResponseDTO response = service.criar(dto);

        assertNotNull(response);
        assertEquals("Rosa", response.nome());

        verify(repoditory).save(produto);
        verify(producer).enviarProdutoCriado(any());
    }
}
