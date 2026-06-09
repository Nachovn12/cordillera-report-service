package cl.duoc.cordillera.reportservice.repository;

import cl.duoc.cordillera.reportservice.model.Reporte;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Capa Repository — pruebas de persistencia con H2 en memoria.
 *
 * @SpringBootTest carga el contexto completo con el perfil "test",
 * que sustituye MySQL por H2 (definido en application-test.properties).
 * @Transactional revierte cada test para mantener aislamiento total.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class ReporteRepositoryTest {

    @Autowired
    private ReporteRepository reporteRepository;

    // -------------------------------------------------------
    // CRUD básico
    // -------------------------------------------------------

    @Test
    void guardar_debeAsignarIdAutoGenerado() {
        // Arrange
        Reporte reporte = reporte("Reporte Ventas Q1", "EJECUTIVO", "Ventas", BigDecimal.valueOf(150000));

        // Act
        Reporte guardado = reporteRepository.save(reporte);

        // Assert
        assertNotNull(guardado.getId(), "El id debe ser generado por la base de datos");
    }

    @Test
    void guardarYFindById_debeRetornarElMismoReporte() {
        // Arrange
        Reporte reporte = reporte("Reporte Inventario", "OPERATIVO", "Inventario", BigDecimal.valueOf(80000));

        // Act
        Reporte guardado = reporteRepository.save(reporte);
        Optional<Reporte> encontrado = reporteRepository.findById(guardado.getId());

        // Assert
        assertTrue(encontrado.isPresent());
        assertEquals("Reporte Inventario", encontrado.get().getTitulo());
        assertEquals("Inventario", encontrado.get().getArea());
        assertEquals(0, BigDecimal.valueOf(80000).compareTo(encontrado.get().getValor()));
    }

    @Test
    void findAll_debeRetornarTodosLosReportesGuardados() {
        // Arrange
        reporteRepository.save(reporte("R-Ventas", "EJECUTIVO", "Ventas", BigDecimal.valueOf(100000)));
        reporteRepository.save(reporte("R-Inventario", "OPERATIVO", "Inventario", BigDecimal.valueOf(200000)));
        reporteRepository.save(reporte("R-Finanzas", "EJECUTIVO", "Finanzas", BigDecimal.valueOf(300000)));

        // Act
        List<Reporte> todos = reporteRepository.findAll();

        // Assert
        assertEquals(3, todos.size());
    }

    @Test
    void eliminar_debeRemoverElReporte_yFindByIdRetornaVacio() {
        // Arrange
        Reporte guardado = reporteRepository.save(
                reporte("Reporte Temporal", "EJECUTIVO", "Logistica", BigDecimal.valueOf(50000)));
        Long id = guardado.getId();

        // Act
        reporteRepository.delete(guardado);
        Optional<Reporte> resultado = reporteRepository.findById(id);

        // Assert
        assertFalse(resultado.isPresent(), "El reporte eliminado no debe encontrarse");
    }

    @Test
    void count_debeReflejarElNumeroRealDeReportesGuardados() {
        // Arrange
        reporteRepository.save(reporte("R1", "EJECUTIVO", "Ventas", BigDecimal.valueOf(1000)));
        reporteRepository.save(reporte("R2", "EJECUTIVO", "CRM", BigDecimal.valueOf(2000)));

        // Act
        long total = reporteRepository.count();

        // Assert
        assertEquals(2, total);
    }

    // -------------------------------------------------------
    // Consulta personalizada: findByArea
    // -------------------------------------------------------

    @Test
    void findByArea_debeRetornarSoloLosReportesDelAreaIndicada() {
        // Arrange
        reporteRepository.save(reporte("Ventas Enero", "EJECUTIVO", "Ventas", BigDecimal.valueOf(100000)));
        reporteRepository.save(reporte("Ventas Febrero", "EJECUTIVO", "Ventas", BigDecimal.valueOf(120000)));
        reporteRepository.save(reporte("Stock Central", "OPERATIVO", "Inventario", BigDecimal.valueOf(80000)));

        // Act
        List<Reporte> resultado = reporteRepository.findByArea("Ventas");

        // Assert
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(r -> "Ventas".equals(r.getArea())));
    }

    @Test
    void findByArea_debeRetornarListaVaciaParaAreaSinReportes() {
        // Arrange
        reporteRepository.save(reporte("Reporte Ventas", "EJECUTIVO", "Ventas", BigDecimal.valueOf(100000)));

        // Act
        List<Reporte> resultado = reporteRepository.findByArea("AreaQueNoExiste");

        // Assert
        assertTrue(resultado.isEmpty());
    }

    // -------------------------------------------------------
    // @PrePersist — fechaGeneracion asignada automáticamente
    // -------------------------------------------------------

    @Test
    void guardar_sinFechaGeneracion_debeAsignarFechaViaPrePersist() {
        // Arrange — no asignamos fechaGeneracion (null)
        Reporte reporte = new Reporte();
        reporte.setTitulo("Sin Fecha");
        reporte.setTipo("EJECUTIVO");
        reporte.setArea("Finanzas");
        reporte.setValor(BigDecimal.valueOf(75000));

        // Act
        Reporte guardado = reporteRepository.save(reporte);

        // Assert
        assertNotNull(guardado.getFechaGeneracion(), "@PrePersist debe asignar fechaGeneracion al persistir");
    }

    @Test
    void guardar_conFechaGeneracion_noDebeModificarla() {
        // Arrange
        LocalDateTime fechaFija = LocalDateTime.of(2026, 1, 15, 10, 30);
        Reporte reporte = reporte("Reporte Fijo", "EJECUTIVO", "CRM", BigDecimal.valueOf(50000));
        reporte.setFechaGeneracion(fechaFija);

        // Act
        Reporte guardado = reporteRepository.save(reporte);

        // Assert
        assertEquals(fechaFija, guardado.getFechaGeneracion(),
                "@PrePersist no debe sobrescribir una fecha ya asignada");
    }

    // -------------------------------------------------------
    // Helper
    // -------------------------------------------------------

    private Reporte reporte(String titulo, String tipo, String area, BigDecimal valor) {
        Reporte r = new Reporte();
        r.setTitulo(titulo);
        r.setTipo(tipo);
        r.setArea(area);
        r.setValor(valor);
        r.setFechaGeneracion(LocalDateTime.now());
        return r;
    }
}
