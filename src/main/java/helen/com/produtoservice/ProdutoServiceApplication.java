package helen.com.produtoservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProdutoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProdutoServiceApplication.class, args);
    }

}
