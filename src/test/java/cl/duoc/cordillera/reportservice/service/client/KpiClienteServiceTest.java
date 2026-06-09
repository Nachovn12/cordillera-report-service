package cl.duoc.cordillera.reportservice.service.client;

import cl.duoc.cordillera.reportservice.dto.KpiResumenDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Capa Client — pruebas de KpiClienteService con Mockito.
 *
 * Se valida:
 * 1. Comportamiento normal cuando el KPI Service responde correctamente.
 * 2. Manejo de respuesta null (servicio devuelve null).
 * 3. Métodos fallback del Circuit Breaker (degradación elegante).
 */
@ExtendWith(MockitoExtension.class)
class KpiClienteServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private KpiClienteService kpiClienteService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kpiClienteService, "kpiServiceUrl", "http://localhost:8084");
    }

    // -------------------------------------------------------
    // obtenerKpis — camino normal
    // -------------------------------------------------------

    @Test
    void obtenerKpis_cuandoServicioRespondeConKpis_debeRetornarLaLista() {
        // Arrange
        KpiResumenDto kpi = kpiDto("Ventas Totales", BigDecimal.valueOf(500000), "ventas");
        KpiResumenDto[] array = { kpi };
        when(restTemplate.getForObject(anyString(), eq(KpiResumenDto[].class))).thenReturn(array);

        // Act
        List<KpiResumenDto> resultado = kpiClienteService.obtenerKpis();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Ventas Totales", resultado.get(0).getNombre());
        verify(restTemplate).getForObject(anyString(), eq(KpiResumenDto[].class));
    }

    @Test
    void obtenerKpis_cuandoServicioRetornaNull_debeRetornarListaVacia() {
        // Arrange — KPI Service devuelve null (deserialización vacía)
        when(restTemplate.getForObject(anyString(), eq(KpiResumenDto[].class))).thenReturn(null);

        // Act
        List<KpiResumenDto> resultado = kpiClienteService.obtenerKpis();

        // Assert
        assertNotNull(resultado, "No debe retornar null");
        assertTrue(resultado.isEmpty(), "Debe retornar lista vacía cuando el servicio retorna null");
    }

    // -------------------------------------------------------
    // obtenerKpisPorCategoria — camino normal
    // -------------------------------------------------------

    @Test
    void obtenerKpisPorCategoria_cuandoServicioResponde_debeRetornarKpisDeLaCategoria() {
        // Arrange
        KpiResumenDto kpi = kpiDto("Stock Disponible", BigDecimal.valueOf(820), "inventario");
        KpiResumenDto[] array = { kpi };
        when(restTemplate.getForObject(anyString(), eq(KpiResumenDto[].class), anyString()))
                .thenReturn(array);

        // Act
        List<KpiResumenDto> resultado = kpiClienteService.obtenerKpisPorCategoria("inventario");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("inventario", resultado.get(0).getCategoria());
    }

    @Test
    void obtenerKpisPorCategoria_cuandoServicioRetornaNull_debeRetornarListaVacia() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(KpiResumenDto[].class), anyString()))
                .thenReturn(null);

        // Act
        List<KpiResumenDto> resultado = kpiClienteService.obtenerKpisPorCategoria("ventas");

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // -------------------------------------------------------
    // Métodos fallback del Circuit Breaker
    // -------------------------------------------------------

    @Test
    void fallbackObtenerKpis_debeRetornarListaVaciaAnteExcepcion() {
        // Arrange — el Circuit Breaker abre y delega al fallback
        RuntimeException causa = new RuntimeException("KPI Service no disponible");

        // Act
        List<KpiResumenDto> resultado = kpiClienteService.fallbackObtenerKpis(causa);

        // Assert
        assertNotNull(resultado, "El fallback no debe retornar null");
        assertTrue(resultado.isEmpty(), "El fallback debe retornar lista vacía");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void fallbackObtenerKpisPorCategoria_debeRetornarListaVaciaAnteExcepcion() {
        // Arrange
        RuntimeException causa = new RuntimeException("Timeout al conectar con KPI Service");

        // Act
        List<KpiResumenDto> resultado = kpiClienteService.fallbackObtenerKpisPorCategoria("logistica", causa);

        // Assert
        assertNotNull(resultado, "El fallback con categoría no debe retornar null");
        assertTrue(resultado.isEmpty(), "El fallback con categoría debe retornar lista vacía");
        verifyNoInteractions(restTemplate);
    }

    // -------------------------------------------------------
    // Helper
    // -------------------------------------------------------

    private KpiResumenDto kpiDto(String nombre, BigDecimal valor, String categoria) {
        return KpiResumenDto.builder()
                .id(1L).nombre(nombre).valor(valor)
                .unidad("CLP").categoria(categoria).estado("OPERATIVO")
                .build();
    }
}
