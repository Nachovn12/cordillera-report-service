package cl.duoc.cordillera.reportservice.service.exportador;

import cl.duoc.cordillera.reportservice.model.Reporte;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class JsonExportador implements Exportador {

  @Override
  public byte[] exportar(Reporte reporte) {
    String contenido = """
        {
          "id": %s,
          "titulo": "%s",
          "tipo": "%s",
          "area": "%s",
          "valor": %s,
          "fechaGeneracion": "%s"
        }
        """.formatted(
        reporte.getId(),
        escapar(reporte.getTitulo()),
        escapar(reporte.getTipo()),
        escapar(reporte.getArea()),
        valorSeguro(reporte.getValor()),
        reporte.getFechaGeneracion());

    return contenido.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public String getContentType() {
    return "application/json";
  }

  @Override
  public String getExtension() {
    return "json";
  }

  private String escapar(String texto) {
    if (texto == null) {
      return "";
    }

    return texto.replace("\\", "\\\\")
        .replace("\"", "\\\"");
  }

  private BigDecimal valorSeguro(BigDecimal valor) {
    return valor == null ? BigDecimal.ZERO : valor;
  }
}
