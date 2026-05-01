package helen.com.produtoservice.metrics;

import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;


@Component
public class ErrorMetrics {
    private final MeterRegistry registry;

    public ErrorMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void increment(String tipoErro) {
        Counter.builder("produto_service_errors_total")
                .description("Total de erros da aplicação")
                .tag("tipo", tipoErro)
                .register(registry)
                .increment();
    }
}
