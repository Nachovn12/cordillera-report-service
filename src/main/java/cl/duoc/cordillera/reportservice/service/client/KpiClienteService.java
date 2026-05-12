package cl.duoc.cordillera.reportservice.service.client;

import cl.duoc.cordillera.reportservice.dto.KpiResumenDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KpiClienteService {

    private final RestTemplate restTemplate;

    @Value("${services.kpi-service.url:http://localhost:8084}")
    private String kpiServiceUrl;

    @CircuitBreaker(name = "kpiService", fallbackMethod = "fallbackObtenerKpis")
    public List<KpiResumenDto> obtenerKpis() {
        KpiResumenDto[] respuesta = restTemplate.getForObject(
                kpiServiceUrl + "/api/kpis",
                KpiResumenDto[].class
        );

        if (respuesta == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(respuesta);
    }

    @CircuitBreaker(name = "kpiService", fallbackMethod = "fallbackObtenerKpisPorCategoria")
    public List<KpiResumenDto> obtenerKpisPorCategoria(String categoria) {
        KpiResumenDto[] respuesta = restTemplate.getForObject(
                kpiServiceUrl + "/api/kpis/categoria/{categoria}",
                KpiResumenDto[].class,
                categoria
        );

        if (respuesta == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(respuesta);
    }

    public List<KpiResumenDto> fallbackObtenerKpis(Throwable throwable) {
        return Collections.emptyList();
    }

    public List<KpiResumenDto> fallbackObtenerKpisPorCategoria(String categoria, Throwable throwable) {
        return Collections.emptyList();
    }
}