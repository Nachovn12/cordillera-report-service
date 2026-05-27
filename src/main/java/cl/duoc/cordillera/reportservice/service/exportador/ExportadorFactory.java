package cl.duoc.cordillera.reportservice.service.exportador;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Component
public class ExportadorFactory {

  public Exportador crearExportador(String formato) {
    if (formato == null || formato.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "El formato de exportación es obligatorio");
    }

    return switch (formato.toLowerCase(Locale.ROOT)) {
      case "pdf" -> new PdfExportador();
      case "excel", "xls", "xlsx" -> new ExcelExportador();
      case "json" -> new JsonExportador();
      default -> throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Formato de exportación no soportado: " + formato);
    };
  }
}
