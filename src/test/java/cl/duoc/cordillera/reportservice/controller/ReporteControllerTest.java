package cl.duoc.cordillera.reportservice.controller;

import cl.duoc.cordillera.reportservice.dto.KpiResumenDto;
import cl.duoc.cordillera.reportservice.model.Reporte;
import cl.duoc.cordillera.reportservice.service.ReporteService;
import cl.duoc.cordillera.reportservice.service.client.KpiClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Capa Controller — pruebas de endpoints REST con MockMvc (standaloneSetup).
 * Patrón AAA (Arrange-Act-Assert). Los beans del servicio están mockeados con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ReporteControllerTest {

    @Mock
    private ReporteService reporteService;

    @Mock
    private KpiClienteService kpiClienteService;

    @InjectMocks
    private ReporteController reporteController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reporteController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    // -------------------------------------------------------
    // GET /api/reportes
    // -------------------------------------------------------

    @Test
    void listarTodos_debeRetornarOkConListaDeReportes() throws Exception {
        // Arrange
        when(reporteService.listarTodos()).thenReturn(List.of(reporte()));

        // Act & Assert
        mockMvc.perform(get("/api/reportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Reporte Ventas"));

        verify(reporteService).listarTodos();
    }

    @Test
    void listarTodos_debeRetornarListaVaciaCuandoNoHayReportes() throws Exception {
        // Arrange
        when(reporteService.listarTodos()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/reportes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(reporteService).listarTodos();
    }

    // -------------------------------------------------------
    // GET /api/reportes/area/{area}
    // -------------------------------------------------------

    @Test
    void listarPorArea_debeRetornarReportesDelAreaIndicada() throws Exception {
        // Arrange
        when(reporteService.listarPorArea("Ventas")).thenReturn(List.of(reporte()));

        // Act & Assert
        mockMvc.perform(get("/api/reportes/area/Ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].area").value("Ventas"));

        verify(reporteService).listarPorArea("Ventas");
    }

    // -------------------------------------------------------
    // GET /api/reportes/{id}
    // -------------------------------------------------------

    @Test
    void buscarPorId_debeRetornarOkCuandoExiste() throws Exception {
        // Arrange
        when(reporteService.buscarPorId(1L)).thenReturn(reporte());

        // Act & Assert
        mockMvc.perform(get("/api/reportes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Reporte Ventas"));

        verify(reporteService).buscarPorId(1L);
    }

    @Test
    void buscarPorId_debeRetornarNotFoundCuandoNoExiste() throws Exception {
        // Arrange
        when(reporteService.buscarPorId(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado con id: 99"));

        // Act & Assert
        mockMvc.perform(get("/api/reportes/99"))
                .andExpect(status().isNotFound());

        verify(reporteService).buscarPorId(99L);
    }

    // -------------------------------------------------------
    // POST /api/reportes
    // -------------------------------------------------------

    @Test
    void crear_debeRetornarCreatedConElReporteGuardado() throws Exception {
        // Arrange
        Reporte r = reporte();
        when(reporteService.crear(any(Reporte.class))).thenReturn(r);

        // Act & Assert
        mockMvc.perform(post("/api/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(reporteService).crear(any(Reporte.class));
    }

    // -------------------------------------------------------
    // POST /api/reportes/generar
    // -------------------------------------------------------

    @Test
    void generar_debeRetornarCreatedConReporteGenerado() throws Exception {
        // Arrange
        Reporte r = reporte();
        when(reporteService.generarReporte(any(Reporte.class))).thenReturn(r);

        // Act & Assert
        mockMvc.perform(post("/api/reportes/generar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Reporte Ventas"));

        verify(reporteService).generarReporte(any(Reporte.class));
    }

    // -------------------------------------------------------
    // PUT /api/reportes/{id}
    // -------------------------------------------------------

    @Test
    void actualizar_debeRetornarOkConDatosActualizados() throws Exception {
        // Arrange
        Reporte r = reporte();
        r.setTitulo("Reporte Actualizado");
        when(reporteService.actualizar(eq(1L), any(Reporte.class))).thenReturn(r);

        // Act & Assert
        mockMvc.perform(put("/api/reportes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Reporte Actualizado"));

        verify(reporteService).actualizar(eq(1L), any(Reporte.class));
    }

    // -------------------------------------------------------
    // DELETE /api/reportes/{id}
    // -------------------------------------------------------

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        // Arrange
        doNothing().when(reporteService).eliminar(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/reportes/1"))
                .andExpect(status().isNoContent());

        verify(reporteService).eliminar(1L);
    }

    // -------------------------------------------------------
    // GET /api/reportes/{id}/exportar — PDF, Excel, JSON
    // -------------------------------------------------------

    @Test
    void exportar_formatoPdf_debeRetornarArchivoPdf() throws Exception {
        // Arrange
        when(reporteService.exportar(1L, "pdf")).thenReturn("contenido-pdf".getBytes());

        // Act & Assert
        mockMvc.perform(get("/api/reportes/1/exportar").param("formato", "pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"reporte-1.pdf\""))
                .andExpect(content().contentType("application/pdf"));

        verify(reporteService).exportar(1L, "pdf");
    }

    @Test
    void exportar_formatoExcel_debeRetornarArchivoXls() throws Exception {
        // Arrange
        when(reporteService.exportar(1L, "excel")).thenReturn("contenido-excel".getBytes());

        // Act & Assert
        mockMvc.perform(get("/api/reportes/1/exportar").param("formato", "excel"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"reporte-1.xls\""))
                .andExpect(content().contentType("application/vnd.ms-excel"));

        verify(reporteService).exportar(1L, "excel");
    }

    @Test
    void exportar_formatoJson_debeRetornarArchivoJson() throws Exception {
        // Arrange
        when(reporteService.exportar(1L, "json")).thenReturn("{\"id\":1}".getBytes());

        // Act & Assert
        mockMvc.perform(get("/api/reportes/1/exportar").param("formato", "json"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"reporte-1.json\""))
                .andExpect(content().contentType("application/json"));

        verify(reporteService).exportar(1L, "json");
    }

    @Test
    void exportar_formatoPorDefecto_esPdf() throws Exception {
        // Arrange — sin parámetro "formato", debe usar "pdf" por defecto
        when(reporteService.exportar(1L, "pdf")).thenReturn("pdf".getBytes());

        // Act & Assert
        mockMvc.perform(get("/api/reportes/1/exportar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));

        verify(reporteService).exportar(1L, "pdf");
    }

    // -------------------------------------------------------
    // GET /api/reportes/kpis
    // -------------------------------------------------------

    @Test
    void listarKpis_debeRetornarOkConListaDeKpis() throws Exception {
        // Arrange
        KpiResumenDto kpi = KpiResumenDto.builder()
                .id(1L).nombre("Ventas Totales").valor(BigDecimal.valueOf(500000))
                .unidad("CLP").categoria("ventas").estado("OPERATIVO").build();
        when(kpiClienteService.obtenerKpis()).thenReturn(List.of(kpi));

        // Act & Assert
        mockMvc.perform(get("/api/reportes/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ventas Totales"));

        verify(kpiClienteService).obtenerKpis();
    }

    @Test
    void listarKpis_debeRetornarListaVaciaCuandoServiceRetornaVacio() throws Exception {
        // Arrange
        when(kpiClienteService.obtenerKpis()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/reportes/kpis"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(kpiClienteService).obtenerKpis();
    }

    @Test
    void listarKpisPorCategoria_debeRetornarKpis() throws Exception {
        // Arrange
        when(kpiClienteService.obtenerKpisPorCategoria("ventas")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/reportes/kpis/categoria/ventas"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(kpiClienteService).obtenerKpisPorCategoria("ventas");
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
        r.setFechaGeneracion(LocalDateTime.of(2026, 5, 23, 12, 0));
        return r;
    }
}
