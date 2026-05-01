package helen.com.produtoservice.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {

    @PostConstruct
    public void init() {
        System.out.println("Logging inicializado com CorrelarionId");
    }
}
