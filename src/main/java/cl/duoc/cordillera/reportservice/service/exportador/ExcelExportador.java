package cl.duoc.cordillera.reportservice.service.exportador;

import cl.duoc.cordillera.reportservice.model.Reporte;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class ExcelExportador implements Exportador {

  @Override
  public byte[] exportar(Reporte reporte) {
    String contenido = """
        \uFEFFID;Titulo;Tipo;Area;Valor;Fecha Generacion
        %s;%s;%s;%s;%s;%s
        """.formatted(
        reporte.getId(),
        valorCsv(reporte.getTitulo()),
        valorCsv(reporte.getTipo()),
        valorCsv(reporte.getArea()),
        valorSeguro(reporte.getValor()),
        reporte.getFechaGeneracion());

    return contenido.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public String getContentType() {
    return "text/csv; charset=UTF-8";
  }

  @Override
  public String getExtension() {
    return "csv";
  }

  private String valorCsv(String texto) {
    if (texto == null) {
      return "\"\"";
    }

    String textoEscapado = texto.replace("\"", "\"\"");
    return "\"" + textoEscapado + "\"";
  }

  private BigDecimal valorSeguro(BigDecimal valor) {
    return valor == null ? BigDecimal.ZERO : valor;
  }
}
