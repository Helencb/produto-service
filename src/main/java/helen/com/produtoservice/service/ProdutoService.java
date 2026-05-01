package helen.com.produtoservice.service;

import helen.com.produtoservice.dto.ProdutoCreateDTO;
import helen.com.produtoservice.dto.ProdutoResponseDTO;
import helen.com.produtoservice.dto.ProdutoUpdateDTO;
import helen.com.produtoservice.exception.ProdutoNotFoundException;
import helen.com.produtoservice.mapper.ProdutoMapper;
import helen.com.produtoservice.messaging.event.ProdutoAtualizadoEvent;
import helen.com.produtoservice.messaging.event.ProdutoCriadoEvent;
import helen.com.produtoservice.messaging.event.ProdutoDesativadoEvent;
import helen.com.produtoservice.messaging.producer.ProdutoProducer;
import helen.com.produtoservice.model.Produto;
import helen.com.produtoservice.model.StatusProduto;
import helen.com.produtoservice.repository.ProdutoRepository;
import helen.com.produtoservice.util.LogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class ProdutoService {
    private final ProdutoRepository repository;
    private final ProdutoProducer producer;
    private final ProdutoMapper mapper;

    @Transactional
    public ProdutoResponseDTO criar(ProdutoCreateDTO dto) {

        log.info("Iniciando criação de produto | correlationId = {} | nome = {}",
                LogUtil.get(),
                dto.nome());

        Produto produto = mapper.toEntity(dto);
        produto.setStatus(StatusProduto.CRIADO);
        produto.setAtivo(true);

        Produto salvo = repository.save(produto);

        log.info("Produto salvo no banco | correlationId={} | id={}",
                LogUtil.get(),
                salvo.getId());

        ProdutoCriadoEvent event = new ProdutoCriadoEvent(
                salvo.getId(),
                salvo.getNome(),
                salvo.getValor(),
                LocalDateTime.now()
        );

        producer.enviarProdutoCriado(event);

        log.info("Evento ProdutoCriado enviado | correlationId={} | id={}",
                LogUtil.get(),
                salvo.getId());

        return mapper.toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listar (Pageable pageable) {
        log.info("Listando produtos | correlationId={}", LogUtil.get());

        return repository.findAllByAtivoTrue(pageable)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(UUID id) {
        log.info("Buscando produto por ID | correlationId={} | id={}",
                LogUtil.get(),
                id);

        Produto produto = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Produto não encontrado | correlationId={} | id={}",
                            LogUtil.get(),
                            id);
                    return new ProdutoNotFoundException(id);
                });
        return mapper.toResponse(produto);
    }

    @Transactional
    public ProdutoResponseDTO atualizar(UUID id, ProdutoUpdateDTO dto) {
        log.info("Atualizando produto | correlationId={} | id={}",
                LogUtil.get(),
                id);

        Produto produto = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Produto não encontrado | correlationId={} | id={}",
                            LogUtil.get(),
                            id);
                    return new ProdutoNotFoundException(id);
                });

        mapper.updateEntity(dto, produto);
        Produto atualizado = repository.save(produto);

        log.info("Produto atualizado no banco | correlationId={} | id={}",
                LogUtil.get(),
                id);

        ProdutoAtualizadoEvent event = new ProdutoAtualizadoEvent(
                atualizado.getId(),
                atualizado.getNome(),
                atualizado.getValor(),
                LocalDateTime.now()
        );

        producer.enviarProdutoAtualizado(event);

        log.info("Evento ProdutoAtualizado enviado | correlationId={} | id={}",
                LogUtil.get(),
                id);

        return mapper.toResponse(atualizado);
    }

    @Transactional
    public void atualizarStatus(UUID id, StatusProduto status) {
        log.info("Atualizando status do produto | correlationId={} | id={} | novoStatus={}",
                LogUtil.get(),
                id,
                status);

        Produto produto = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Produto não encontrado para atualizar status | correlationId={} | id={}",
                            LogUtil.get(),
                            id);
                    return new ProdutoNotFoundException(id);
                });

        produto.setStatus(status);
        if(status == StatusProduto.SEM_ESTOQUE) {
            produto.setAtivo(false);
        }

        repository.save(produto);
        log.info("Status atualizado com sucesso | correlationId={} | id={}",
                LogUtil.get(),
                id);
    }

    @Transactional
    public void deletar(UUID id) {
        log.info("Desativando produto | correlationId={} | id={}",
                LogUtil.get(),
                id);

        Produto produto = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Produto não encontrado para deletar | correlationId={} | id={}",
                            LogUtil.get(),
                            id);
                    return new ProdutoNotFoundException(id);
                });
        produto.setAtivo(false);
        repository.save(produto);

        log.info("Produto desativado no banco | correlationId={} | id={}",
                LogUtil.get(),
                id);

        ProdutoDesativadoEvent event = new ProdutoDesativadoEvent(
                id,
                LocalDateTime.now()
        );

        producer.enviarProdutoDesativado(event);

        log.info("Evento ProdutoDesativado enviado | correlationId={} | id={}",
                LogUtil.get(),
                id);

    }
}

