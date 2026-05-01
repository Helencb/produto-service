package helen.com.produtoservice;

import helen.com.produtoservice.config.RabbitTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(RabbitTestConfig.class)
class ProdutoServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
