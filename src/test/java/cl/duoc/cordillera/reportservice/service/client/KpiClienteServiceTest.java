package cl.duoc.cordillera.reportservice.service.client;

import cl.duoc.cordillera.reportservice.dto.KpiResumenDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class KpiClienteServiceTest {

    private KpiClienteService kpiClienteService;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        kpiClienteService = new KpiClienteService(restTemplate);
    }

    @Test
    void fallbackObtenerKpisDebeRetornarListaVacia() {
        List<KpiResumenDto> resultado = kpiClienteService.fallbackObtenerKpis(
                new RuntimeException("KPI Service no disponible")
        );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void fallbackObtenerKpisPorCategoriaDebeRetornarListaVacia() {
        List<KpiResumenDto> resultado = kpiClienteService.fallbackObtenerKpisPorCategoria(
                "ventas",
                new RuntimeException("KPI Service no disponible")
        );

        assertTrue(resultado.isEmpty());
    }
}
