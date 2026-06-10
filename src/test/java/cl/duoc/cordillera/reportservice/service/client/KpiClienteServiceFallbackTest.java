package cl.duoc.cordillera.reportservice.service.client;

import cl.duoc.cordillera.reportservice.dto.KpiResumenDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test del fallback del Circuit Breaker en KpiClienteService.
 *
 * El Circuit Breaker (Resilience4j) se activa cuando el KPI Service no está disponible.
 * Los métodos fallback garantizan que el Report Service siga funcionando
 * aunque el KPI Service falle (degradación elegante).
 */
class KpiClienteServiceFallbackTest {

    private KpiClienteService kpiClienteService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        kpiClienteService = new KpiClienteService(restTemplate);
        ReflectionTestUtils.setField(kpiClienteService, "kpiServiceUrl", "http://localhost:8084");
    }

    @Test
    void fallbackObtenerKpis_debeRetornarListaVacia() {
        // El fallback es invocado cuando el Circuit Breaker está abierto
        RuntimeException causa = new RuntimeException("KPI Service no disponible");

        List<KpiResumenDto> resultado = kpiClienteService.fallbackObtenerKpis(causa);

        assertNotNull(resultado, "El fallback no debe retornar null");
        assertTrue(resultado.isEmpty(), "El fallback debe retornar lista vacía ante fallo del Circuit Breaker");
    }

    @Test
    void fallbackObtenerKpisPorCategoria_debeRetornarListaVacia() {
        RuntimeException causa = new RuntimeException("Timeout al conectar con KPI Service");

        List<KpiResumenDto> resultado = kpiClienteService.fallbackObtenerKpisPorCategoria("ventas", causa);

        assertNotNull(resultado, "El fallback no debe retornar null");
        assertTrue(resultado.isEmpty(), "El fallback con categoría debe retornar lista vacía");
    }

    @Test
    void obtenerKpis_cuandoServicioNoDisponible_debeRetornarListaVacia() {
        // Simula que el KPI Service rechaza la conexión (Circuit Breaker no activo en unit test,
        // pero el servicio retorna null — se valida el camino de null handling)
        when(restTemplate.getForObject(anyString(), any())).thenReturn(null);

        List<KpiResumenDto> resultado = kpiClienteService.obtenerKpis();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty(), "Debe retornar lista vacía cuando KPI Service retorna null");
    }

    @Test
    void obtenerKpisPorCategoria_cuandoServicioRetornaKpis_debeRetornarLista() {
        KpiResumenDto kpi = new KpiResumenDto();
        kpi.setNombre("Ventas Totales");
        kpi.setValor(BigDecimal.valueOf(500000));
        kpi.setCategoria("ventas");
        KpiResumenDto[] kpisArray = { kpi };

        when(restTemplate.getForObject(anyString(), any(), anyString())).thenReturn(kpisArray);

        List<KpiResumenDto> resultado = kpiClienteService.obtenerKpisPorCategoria("ventas");

        assertNotNull(resultado);
        assertTrue(resultado.size() == 1);
    }
}
