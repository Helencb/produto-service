package helen.com.produtoservice.mapper;

import helen.com.produtoservice.dto.ProdutoCreateDTO;
import helen.com.produtoservice.dto.ProdutoResponseDTO;
import helen.com.produtoservice.dto.ProdutoUpdateDTO;
import helen.com.produtoservice.model.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {
    Produto toEntity(ProdutoCreateDTO dto);

    ProdutoResponseDTO toResponse(Produto produto);

    void updateEntity(ProdutoUpdateDTO dto, @MappingTarget Produto produto);
}
