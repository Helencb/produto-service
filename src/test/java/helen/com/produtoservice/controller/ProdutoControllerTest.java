package helen.com.produtoservice.controller;

import helen.com.produtoservice.dto.ProdutoCreateDTO;
import helen.com.produtoservice.dto.ProdutoResponseDTO;
import helen.com.produtoservice.model.StatusProduto;
import helen.com.produtoservice.service.ProdutoService;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
@TestPropertySource(properties = {
        "app.api.pagination.default-size=10",
        "app.api.pagination.max-size=50"
})
public class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void deveCriarProduto() throws Exception {
        UUID id = UUID.randomUUID();

        ProdutoCreateDTO dto = new ProdutoCreateDTO("Rosa", "Flor vermelha", new BigDecimal("10.00"));

        ProdutoResponseDTO response = new ProdutoResponseDTO(id, "Rosa", new BigDecimal("10.00"), StatusProduto.CRIADO, true);

        when(service.criar(any())).thenReturn(response);

        mockMvc.perform(post("/produtos")
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.nome").value("Rosa"));
    }
}
