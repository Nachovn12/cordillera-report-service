package cl.duoc.cordillera.reportservice.controller;

import cl.duoc.cordillera.reportservice.model.Reporte;
import cl.duoc.cordillera.reportservice.service.ReporteService;
import cl.duoc.cordillera.reportservice.service.client.KpiClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReporteControllerTest {

  private ReporteService reporteService;
  private KpiClienteService kpiClienteService;
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    reporteService = mock(ReporteService.class);
    kpiClienteService = mock(KpiClienteService.class);

    ReporteController controller = new ReporteController(
        reporteService,
        kpiClienteService);

    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
  }

  @Test
  void listarTodosDebeRetornarOk() throws Exception {
    when(reporteService.listarTodos()).thenReturn(List.of(crearReporte()));

    mockMvc.perform(get("/api/reportes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].titulo").value("Reporte Ventas"));

    verify(reporteService).listarTodos();
  }

  @Test
  void listarPorAreaDebeRetornarOk() throws Exception {
    when(reporteService.listarPorArea("Ventas")).thenReturn(List.of(crearReporte()));

    mockMvc.perform(get("/api/reportes/area/Ventas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].area").value("Ventas"));

    verify(reporteService).listarPorArea("Ventas");
  }

  @Test
  void buscarPorIdDebeRetornarOk() throws Exception {
    when(reporteService.buscarPorId(1L)).thenReturn(crearReporte());

    mockMvc.perform(get("/api/reportes/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.titulo").value("Reporte Ventas"));

    verify(reporteService).buscarPorId(1L);
  }

  @Test
  void crearDebeRetornarCreated() throws Exception {
    Reporte reporte = crearReporte();
    when(reporteService.crear(any(Reporte.class))).thenReturn(reporte);

    mockMvc.perform(post("/api/reportes")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(reporte)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1));

    verify(reporteService).crear(any(Reporte.class));
  }

  @Test
  void generarDebeRetornarCreated() throws Exception {
    Reporte reporte = crearReporte();
    when(reporteService.generarReporte(any(Reporte.class))).thenReturn(reporte);

    mockMvc.perform(post("/api/reportes/generar")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(reporte)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.titulo").value("Reporte Ventas"));

    verify(reporteService).generarReporte(any(Reporte.class));
  }

  @Test
  void actualizarDebeRetornarOk() throws Exception {
    Reporte reporte = crearReporte();
    reporte.setTitulo("Reporte Actualizado");

    when(reporteService.actualizar(eq(1L), any(Reporte.class))).thenReturn(reporte);

    mockMvc.perform(put("/api/reportes/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(reporte)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.titulo").value("Reporte Actualizado"));

    verify(reporteService).actualizar(eq(1L), any(Reporte.class));
  }

  @Test
  void eliminarDebeRetornarNoContent() throws Exception {
    doNothing().when(reporteService).eliminar(1L);

    mockMvc.perform(delete("/api/reportes/1"))
        .andExpect(status().isNoContent());

    verify(reporteService).eliminar(1L);
  }

  @Test
  void exportarDebeRetornarArchivo() throws Exception {
    when(reporteService.exportar(1L, "pdf")).thenReturn("contenido-pdf".getBytes());

    mockMvc.perform(get("/api/reportes/1/exportar")
        .param("formato", "pdf"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition", "attachment; filename=\"reporte-1.pdf\""))
        .andExpect(content().contentType("application/pdf"))
        .andExpect(content().bytes("contenido-pdf".getBytes()));

    verify(reporteService).exportar(1L, "pdf");
  }

  @Test
  void listarKpisDebeRetornarOk() throws Exception {
    when(kpiClienteService.obtenerKpis()).thenReturn(List.of());

    mockMvc.perform(get("/api/reportes/kpis"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(kpiClienteService).obtenerKpis();
  }

  @Test
  void listarKpisPorCategoriaDebeRetornarOk() throws Exception {
    when(kpiClienteService.obtenerKpisPorCategoria("ventas")).thenReturn(List.of());

    mockMvc.perform(get("/api/reportes/kpis/categoria/ventas"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(kpiClienteService).obtenerKpisPorCategoria("ventas");
  }

  private Reporte crearReporte() {
    Reporte reporte = new Reporte();
    reporte.setId(1L);
    reporte.setTitulo("Reporte Ventas");
    reporte.setTipo("EJECUTIVO");
    reporte.setArea("Ventas");
    reporte.setValor(BigDecimal.valueOf(150000));
    reporte.setFechaGeneracion(LocalDateTime.of(2026, 5, 23, 12, 0));
    return reporte;
  }
}
