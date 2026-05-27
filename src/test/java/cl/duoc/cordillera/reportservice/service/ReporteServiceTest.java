package cl.duoc.cordillera.reportservice.service;

import cl.duoc.cordillera.reportservice.model.Reporte;
import cl.duoc.cordillera.reportservice.repository.ReporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import cl.duoc.cordillera.reportservice.service.exportador.Exportador;
import cl.duoc.cordillera.reportservice.service.exportador.ExportadorFactory;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private ExportadorFactory exportadorFactory;

    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteService = new ReporteService(reporteRepository, exportadorFactory);
    }

    @Test
    void listarTodosDebeRetornarReportes() {
        Reporte reporte = crearReporte();
        when(reporteRepository.findAll()).thenReturn(List.of(reporte));

        List<Reporte> resultado = reporteService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Reporte Ventas", resultado.get(0).getTitulo());
        verify(reporteRepository).findAll();
    }

    @Test
    void buscarPorIdDebeRetornarReporteCuandoExiste() {
        Reporte reporte = crearReporte();
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        Reporte resultado = reporteService.buscarPorId(1L);

        assertEquals("Reporte Ventas", resultado.getTitulo());
        verify(reporteRepository).findById(1L);
    }

    @Test
    void buscarPorIdDebeLanzarNotFoundCuandoNoExiste() {
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reporteService.buscarPorId(99L)
        );

        assertEquals(404, ex.getStatusCode().value());
        verify(reporteRepository).findById(99L);
    }

    @Test
    void crearDebeLimpiarIdYGuardarReporte() {
        Reporte reporte = crearReporte();
        reporte.setId(10L);

        when(reporteRepository.save(any(Reporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reporte resultado = reporteService.crear(reporte);

        assertNull(resultado.getId());
        verify(reporteRepository).save(reporte);
    }

    @Test
    void generarReporteDebeCompletarCamposPorDefecto() {
        Reporte reporte = new Reporte();
        reporte.setArea("Ventas");
        reporte.setValor(BigDecimal.valueOf(150000));

        when(reporteRepository.save(any(Reporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reporte resultado = reporteService.generarReporte(reporte);

        assertEquals("Reporte ejecutivo de Ventas", resultado.getTitulo());
        assertEquals("EJECUTIVO", resultado.getTipo());
        assertNotNull(resultado.getFechaGeneracion());
        verify(reporteRepository).save(reporte);
    }

    @Test
    void generarReporteDebeLanzarBadRequestCuandoAreaVieneVacia() {
        Reporte reporte = new Reporte();
        reporte.setArea("");
        reporte.setValor(BigDecimal.valueOf(1000));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reporteService.generarReporte(reporte)
        );

        assertEquals(400, ex.getStatusCode().value());
        verify(reporteRepository, never()).save(any());
    }

    @Test
    void generarReporteDebeLanzarBadRequestCuandoValorEsNegativo() {
        Reporte reporte = new Reporte();
        reporte.setArea("Finanzas");
        reporte.setValor(BigDecimal.valueOf(-1));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reporteService.generarReporte(reporte)
        );

        assertEquals(400, ex.getStatusCode().value());
        verify(reporteRepository, never()).save(any());
    }

    @Test
    void actualizarDebeModificarReporteExistente() {
        Reporte existente = crearReporte();
        Reporte cambios = crearReporte();
        cambios.setTitulo("Reporte Actualizado");
        cambios.setArea("Inventario");
        cambios.setValor(BigDecimal.valueOf(300000));

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reporte resultado = reporteService.actualizar(1L, cambios);

        assertEquals("Reporte Actualizado", resultado.getTitulo());
        assertEquals("Inventario", resultado.getArea());
        assertEquals(BigDecimal.valueOf(300000), resultado.getValor());
        verify(reporteRepository).save(existente);
    }

    @Test
    void eliminarDebeBuscarYEliminarReporte() {
        Reporte reporte = crearReporte();
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        reporteService.eliminar(1L);

        verify(reporteRepository).delete(reporte);
    }

    @Test
    void listarPorAreaDebeConsultarRepositorio() {
        Reporte reporte = crearReporte();
        when(reporteRepository.findByArea("Ventas")).thenReturn(List.of(reporte));

        List<Reporte> resultado = reporteService.listarPorArea("Ventas");

        assertEquals(1, resultado.size());
        verify(reporteRepository).findByArea("Ventas");
    }

    @Test
    void exportarDebeUsarFactoryYRetornarBytes() {
        Reporte reporte = crearReporte();
        Exportador exportador = mock(Exportador.class);

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(exportadorFactory.crearExportador("pdf")).thenReturn(exportador);
        when(exportador.exportar(reporte)).thenReturn("bytes".getBytes());

        byte[] resultado = reporteService.exportar(1L, "pdf");

        assertArrayEquals("bytes".getBytes(), resultado);
        verify(exportador).exportar(reporte);
    }

    private Reporte crearReporte() {
        Reporte reporte = new Reporte();
        reporte.setId(1L);
        reporte.setTitulo("Reporte Ventas");
        reporte.setTipo("EJECUTIVO");
        reporte.setArea("Ventas");
        reporte.setValor(BigDecimal.valueOf(150000));
        reporte.setFechaGeneracion(LocalDateTime.now());
        return reporte;
    }
}
