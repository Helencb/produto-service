package helen.com.produtoservice.controller;

import helen.com.produtoservice.dto.*;
import helen.com.produtoservice.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Gerenciamento de produtos")
public class ProdutoController {
    private final ProdutoService service;

    @Value("${app.api.pagination.default-size}")
    private int defaultPageSize;

    @Value("${app.api.pagination.max-size}")
    private int maxPageSize;

    @PostMapping
    @Operation(summary = "Cria um novo produto")
    public ResponseEntity<ApiResponse<ProdutoResponseDTO>> criar(@RequestBody @Valid ProdutoCreateDTO dto) {
        ProdutoResponseDTO response = service.criar(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location)
                .body(new ApiResponse<>(true, "Produto criado com sucesso", response));
    }

    @GetMapping
    @Operation(summary = "Lista produtos com paginacao controlada")
    public ResponseEntity<ApiResponse<PageResponse<ProdutoResponseDTO>>> listar(@RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(required = false) Integer size,
                                                                                @RequestParam(defaultValue = "id") String sort,
                                                                                @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        int resolvedSize = size == null ? defaultPageSize : Math.min(Math.max(size, 1), maxPageSize);
        PageRequest pageRequest = PageRequest.of(page, resolvedSize, Sort.by(direction, sort));
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Lista de pedidos",
                        PageResponse.from(service.listar(pageRequest))
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um produto por id")
    public ResponseEntity<ApiResponse<ProdutoResponseDTO>> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Produto encontrado", service.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Substitui completamente um produto ainda editavel")
    public ResponseEntity<ApiResponse<ProdutoResponseDTO>> atualizar(@PathVariable UUID id,
                                                                     @RequestBody @Valid ProdutoUpdateDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Produto atualizado", service.atualizar(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela um produto quando permitido")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Produto cancelado", null));
    }
}
