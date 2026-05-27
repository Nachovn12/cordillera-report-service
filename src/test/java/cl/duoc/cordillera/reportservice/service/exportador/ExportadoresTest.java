package cl.duoc.cordillera.reportservice.service.exportador;

import cl.duoc.cordillera.reportservice.model.Reporte;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExportadoresTest {

  @Test
  void pdfExportadorDebeGenerarReporteEjecutivo() {
    Reporte reporte = crearReporte();
    PdfExportador exportador = new PdfExportador();

    byte[] resultado = exportador.exportar(reporte);
    String contenido = new String(resultado, StandardCharsets.US_ASCII);

    assertTrue(contenido.startsWith("%PDF-1.4"));
    assertTrue(contenido.contains("CORDILLERA Platform"));
    assertTrue(contenido.contains("Reporte Ejecutivo Cordillera Platform"));
    assertTrue(contenido.contains("Resumen ejecutivo"));
    assertTrue(contenido.contains("KPI principal"));
    assertTrue(contenido.contains("$150.000 CLP"));
    assertTrue(contenido.contains("Detalle del reporte"));
    assertTrue(contenido.contains("Observacion"));
    assertTrue(contenido.contains("Cordillera Platform - Report Service - Documento generado automaticamente"));
    assertTrue(contenido.contains("xref"));
    assertTrue(contenido.contains("%%EOF"));
    assertEquals("application/pdf", exportador.getContentType());
    assertEquals("pdf", exportador.getExtension());
  }

  @Test
  void pdfExportadorDebeEscaparParentesisBackslashYNormalizarTexto() {
    Reporte reporte = crearReporte();
    reporte.setTitulo("Título (Especial) \\ Ñ");
    reporte.setTipo("Ejecutivo");
    reporte.setArea("Finanzas");

    PdfExportador exportador = new PdfExportador();

    String contenido = new String(exportador.exportar(reporte), StandardCharsets.US_ASCII);

    assertTrue(contenido.contains("Titulo \\(Especial\\) \\\\ N"));
  }

  @Test
  void pdfExportadorDebeManejarCamposNulos() {
    Reporte reporte = new Reporte();
    reporte.setId(2L);
    reporte.setTitulo(null);
    reporte.setTipo(null);
    reporte.setArea(null);
    reporte.setValor(null);
    reporte.setFechaGeneracion(null);

    PdfExportador exportador = new PdfExportador();

    String contenido = new String(exportador.exportar(reporte), StandardCharsets.US_ASCII);

    assertTrue(contenido.contains("$0 CLP"));
    assertTrue(contenido.contains("Sin fecha"));
  }

  @Test
  void excelExportadorDebeGenerarCsvUtf8() {
    Reporte reporte = crearReporte();
    ExcelExportador exportador = new ExcelExportador();

    String contenido = new String(exportador.exportar(reporte), StandardCharsets.UTF_8);

    assertTrue(contenido.contains("ID;Titulo;Tipo;Area;Valor;Fecha Generacion"));
    assertTrue(contenido.contains("1;\"Reporte Ventas\";\"EJECUTIVO\";\"Ventas\";150000"));
    assertEquals("application/vnd.ms-excel", exportador.getContentType());
    assertEquals("xls", exportador.getExtension());
  }

  @Test
  void excelExportadorDebeEscaparComillasYValoresNulos() {
    Reporte reporte = new Reporte();
    reporte.setId(3L);
    reporte.setTitulo("Reporte \"Especial\"");
    reporte.setTipo(null);
    reporte.setArea(null);
    reporte.setValor(null);
    reporte.setFechaGeneracion(null);

    ExcelExportador exportador = new ExcelExportador();

    String contenido = new String(exportador.exportar(reporte), StandardCharsets.UTF_8);

    assertTrue(contenido.contains("\"Reporte \"\"Especial\"\"\""));
    assertTrue(contenido.contains("\"\";\"\";0;null"));
  }

  @Test
  void jsonExportadorDebeGenerarJson() {
    Reporte reporte = crearReporte();
    JsonExportador exportador = new JsonExportador();

    String contenido = new String(exportador.exportar(reporte), StandardCharsets.UTF_8);

    assertTrue(contenido.contains("\"id\": 1"));
    assertTrue(contenido.contains("\"titulo\": \"Reporte Ventas\""));
    assertTrue(contenido.contains("\"tipo\": \"EJECUTIVO\""));
    assertTrue(contenido.contains("\"area\": \"Ventas\""));
    assertTrue(contenido.contains("\"valor\": 150000"));
    assertEquals("application/json", exportador.getContentType());
    assertEquals("json", exportador.getExtension());
  }

  @Test
  void jsonExportadorDebeEscaparTextoYValoresNulos() {
    Reporte reporte = new Reporte();
    reporte.setId(4L);
    reporte.setTitulo("Reporte \"Especial\" \\ Demo");
    reporte.setTipo(null);
    reporte.setArea(null);
    reporte.setValor(null);
    reporte.setFechaGeneracion(null);

    JsonExportador exportador = new JsonExportador();

    String contenido = new String(exportador.exportar(reporte), StandardCharsets.UTF_8);

    assertTrue(contenido.contains("Reporte \\\"Especial\\\" \\\\ Demo"));
    assertTrue(contenido.contains("\"tipo\": \"\""));
    assertTrue(contenido.contains("\"area\": \"\""));
    assertTrue(contenido.contains("\"valor\": 0"));
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
