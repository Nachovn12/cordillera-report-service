package cl.duoc.cordillera.reportservice.service;

import cl.duoc.cordillera.reportservice.model.Reporte;
import cl.duoc.cordillera.reportservice.repository.ReporteRepository;
import cl.duoc.cordillera.reportservice.service.exportador.Exportador;
import cl.duoc.cordillera.reportservice.service.exportador.ExportadorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Capa Service — pruebas de lógica de negocio con Mockito.
 * Patrón AAA. El repositorio y la factory están mockeados para aislar la lógica.
 */
@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private ExportadorFactory exportadorFactory;

    @InjectMocks
    private ReporteService reporteService;

    // -------------------------------------------------------
    // listarTodos
    // -------------------------------------------------------

    @Test
    void listarTodos_debeRetornarTodosLosReportes() {
        // Arrange
        when(reporteRepository.findAll()).thenReturn(List.of(reporte()));

        // Act
        List<Reporte> resultado = reporteService.listarTodos();

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Reporte Ventas", resultado.get(0).getTitulo());
        verify(reporteRepository).findAll();
    }

    // -------------------------------------------------------
    // buscarPorId
    // -------------------------------------------------------

    @Test
    void buscarPorId_debeRetornarReporteCuandoExiste() {
        // Arrange
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte()));

        // Act
        Reporte resultado = reporteService.buscarPorId(1L);

        // Assert
        assertEquals("Reporte Ventas", resultado.getTitulo());
        verify(reporteRepository).findById(1L);
    }

    @Test
    void buscarPorId_debeLanzarNotFound404CuandoNoExiste() {
        // Arrange
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reporteService.buscarPorId(99L));

        assertEquals(404, ex.getStatusCode().value());
        verify(reporteRepository).findById(99L);
    }

    // -------------------------------------------------------
    // crear
    // -------------------------------------------------------

    @Test
    void crear_debeLimpiarIdYGuardarElReporte() {
        // Arrange
        Reporte r = reporte();
        r.setId(99L);
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Reporte resultado = reporteService.crear(r);

        // Assert
        assertNull(resultado.getId(), "crear() debe limpiar el id antes de guardar");
        verify(reporteRepository).save(r);
    }

    // -------------------------------------------------------
    // generarReporte
    // -------------------------------------------------------

    @Test
    void generarReporte_debeCompletarCamposPorDefectoCuandoVierenVacios() {
        // Arrange
        Reporte r = new Reporte();
        r.setArea("Ventas");
        r.setValor(BigDecimal.valueOf(150000));
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Reporte resultado = reporteService.generarReporte(r);

        // Assert
        assertEquals("Reporte ejecutivo de Ventas", resultado.getTitulo());
        assertEquals("EJECUTIVO", resultado.getTipo());
        assertNotNull(resultado.getFechaGeneracion());
        verify(reporteRepository).save(r);
    }

    @Test
    void generarReporte_noDebeModificarTituloNiTipoCuandoYaVienen() {
        // Arrange
        Reporte r = new Reporte();
        r.setArea("Inventario");
        r.setValor(BigDecimal.valueOf(50000));
        r.setTitulo("Mi reporte personalizado");
        r.setTipo("OPERATIVO");
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Reporte resultado = reporteService.generarReporte(r);

        // Assert
        assertEquals("Mi reporte personalizado", resultado.getTitulo());
        assertEquals("OPERATIVO", resultado.getTipo());
    }

    @Test
    void generarReporte_debeLanzarBadRequest400CuandoAreaEsVacia() {
        // Arrange
        Reporte r = new Reporte();
        r.setArea("");
        r.setValor(BigDecimal.valueOf(1000));

        // Act & Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reporteService.generarReporte(r));

        assertEquals(400, ex.getStatusCode().value());
        verify(reporteRepository, never()).save(any());
    }

    @Test
    void generarReporte_debeLanzarBadRequest400CuandoValorEsNulo() {
        // Arrange
        Reporte r = new Reporte();
        r.setArea("Finanzas");
        r.setValor(null);

        // Act & Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reporteService.generarReporte(r));

        assertEquals(400, ex.getStatusCode().value());
        verify(reporteRepository, never()).save(any());
    }

    @Test
    void generarReporte_debeLanzarBadRequest400CuandoValorEsNegativo() {
        // Arrange
        Reporte r = new Reporte();
        r.setArea("Finanzas");
        r.setValor(BigDecimal.valueOf(-1));

        // Act & Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reporteService.generarReporte(r));

        assertEquals(400, ex.getStatusCode().value());
        verify(reporteRepository, never()).save(any());
    }

    @Test
    void generarReporte_noDebeLanzarExcepcionCuandoValorEsCero() {
        // Arrange — valor = 0 es válido (@PositiveOrZero)
        Reporte r = new Reporte();
        r.setArea("Logistica");
        r.setValor(BigDecimal.ZERO);
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act & Assert — no debe lanzar excepción
        assertDoesNotThrow(() -> reporteService.generarReporte(r));
        verify(reporteRepository).save(r);
    }

    // -------------------------------------------------------
    // actualizar
    // -------------------------------------------------------

    @Test
    void actualizar_debeModificarCamposDelReporteExistente() {
        // Arrange
        Reporte existente = reporte();
        Reporte cambios = new Reporte();
        cambios.setTitulo("Reporte Actualizado");
        cambios.setTipo("OPERATIVO");
        cambios.setArea("Inventario");
        cambios.setValor(BigDecimal.valueOf(300000));

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Reporte resultado = reporteService.actualizar(1L, cambios);

        // Assert
        assertEquals("Reporte Actualizado", resultado.getTitulo());
        assertEquals("OPERATIVO", resultado.getTipo());
        assertEquals("Inventario", resultado.getArea());
        assertEquals(BigDecimal.valueOf(300000), resultado.getValor());
        verify(reporteRepository).save(existente);
    }

    @Test
    void actualizar_debeActualizarFechaGeneracionCuandoVieneEnCambios() {
        // Arrange
        Reporte existente = reporte();
        Reporte cambios = reporte();
        LocalDateTime nuevaFecha = LocalDateTime.of(2026, 6, 1, 9, 0);
        cambios.setFechaGeneracion(nuevaFecha);

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Reporte resultado = reporteService.actualizar(1L, cambios);

        // Assert
        assertEquals(nuevaFecha, resultado.getFechaGeneracion());
    }

    @Test
    void actualizar_debeLanzarNotFound404SiReporteNoExiste() {
        // Arrange
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reporteService.actualizar(99L, reporte()));

        assertEquals(404, ex.getStatusCode().value());
        verify(reporteRepository, never()).save(any());
    }

    // -------------------------------------------------------
    // eliminar
    // -------------------------------------------------------

    @Test
    void eliminar_debeBuscarElReporteYLuegoBorrarlo() {
        // Arrange
        Reporte r = reporte();
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(r));

        // Act
        reporteService.eliminar(1L);

        // Assert
        verify(reporteRepository).delete(r);
    }

    @Test
    void eliminar_debeLanzarNotFound404SiNoExiste() {
        // Arrange
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reporteService.eliminar(99L));

        assertEquals(404, ex.getStatusCode().value());
        verify(reporteRepository, never()).delete(any());
    }

    // -------------------------------------------------------
    // listarPorArea
    // -------------------------------------------------------

    @Test
    void listarPorArea_debeConsultarElRepositorioConElAreaIndicada() {
        // Arrange
        when(reporteRepository.findByArea("Ventas")).thenReturn(List.of(reporte()));

        // Act
        List<Reporte> resultado = reporteService.listarPorArea("Ventas");

        // Assert
        assertEquals(1, resultado.size());
        verify(reporteRepository).findByArea("Ventas");
    }

    // -------------------------------------------------------
    // exportar — Factory Method pattern
    // -------------------------------------------------------

    @Test
    void exportar_debeUsarExportadorFactoryYRetornarBytes() {
        // Arrange
        Reporte r = reporte();
        Exportador exportadorMock = mock(Exportador.class);

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(r));
        when(exportadorFactory.crearExportador("pdf")).thenReturn(exportadorMock);
        when(exportadorMock.exportar(r)).thenReturn("bytes-pdf".getBytes());

        // Act
        byte[] resultado = reporteService.exportar(1L, "pdf");

        // Assert
        assertArrayEquals("bytes-pdf".getBytes(), resultado);
        verify(exportadorFactory).crearExportador("pdf");
        verify(exportadorMock).exportar(r);
    }

    @Test
    void exportar_debeDelegarFormatoExcelALaFactory() {
        // Arrange
        Reporte r = reporte();
        Exportador exportadorMock = mock(Exportador.class);

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(r));
        when(exportadorFactory.crearExportador("excel")).thenReturn(exportadorMock);
        when(exportadorMock.exportar(r)).thenReturn("bytes-excel".getBytes());

        // Act
        byte[] resultado = reporteService.exportar(1L, "excel");

        // Assert
        assertNotNull(resultado);
        verify(exportadorFactory).crearExportador("excel");
    }

    @Test
    void exportar_debeLanzarNotFound404SiElReporteNoExiste() {
        // Arrange
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reporteService.exportar(99L, "pdf"));

        assertEquals(404, ex.getStatusCode().value());
        verify(exportadorFactory, never()).crearExportador(any());
    }

    // -------------------------------------------------------
    // Helper
    // -------------------------------------------------------

    private Reporte reporte() {
        Reporte r = new Reporte();
        r.setId(1L);
        r.setTitulo("Reporte Ventas");
        r.setTipo("EJECUTIVO");
        r.setArea("Ventas");
        r.setValor(BigDecimal.valueOf(150000));
        r.setFechaGeneracion(LocalDateTime.now());
        return r;
    }
}
