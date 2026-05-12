package cl.duoc.cordillera.reportservice.service.exportador;

import cl.duoc.cordillera.reportservice.model.Reporte;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class PdfExportador implements Exportador {

  @Override
  public byte[] exportar(Reporte reporte) {
    List<String> lineas = List.of(
        "Reporte Ejecutivo Cordillera Platform",
        "",
        "ID: " + reporte.getId(),
        "Titulo: " + textoSeguro(reporte.getTitulo()),
        "Tipo: " + textoSeguro(reporte.getTipo()),
        "Area: " + textoSeguro(reporte.getArea()),
        "Valor: " + valorSeguro(reporte.getValor()),
        "Fecha Generacion: " + reporte.getFechaGeneracion());

    return crearPdfBasico(lineas);
  }

  @Override
  public String getContentType() {
    return "application/pdf";
  }

  @Override
  public String getExtension() {
    return "pdf";
  }

  private byte[] crearPdfBasico(List<String> lineas) {
    StringBuilder stream = new StringBuilder();

    stream.append("BT\n");
    stream.append("/F1 12 Tf\n");
    stream.append("50 740 Td\n");

    for (int i = 0; i < lineas.size(); i++) {
      if (i > 0) {
        stream.append("0 -20 Td\n");
      }

      stream.append("(")
          .append(escaparPdf(normalizarAscii(lineas.get(i))))
          .append(") Tj\n");
    }

    stream.append("ET\n");

    String streamTexto = stream.toString();

    List<String> objetos = List.of(
        "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
        "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n",
        "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n",
        "4 0 obj\n<< /Length " + streamTexto.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n" + streamTexto
            + "endstream\nendobj\n",
        "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

    StringBuilder pdf = new StringBuilder();
    List<Integer> offsets = new ArrayList<>();

    pdf.append("%PDF-1.4\n");

    for (String objeto : objetos) {
      offsets.add(pdf.toString().getBytes(StandardCharsets.US_ASCII).length);
      pdf.append(objeto);
    }

    int inicioXref = pdf.toString().getBytes(StandardCharsets.US_ASCII).length;

    pdf.append("xref\n");
    pdf.append("0 ").append(objetos.size() + 1).append("\n");
    pdf.append("0000000000 65535 f \n");

    for (Integer offset : offsets) {
      pdf.append(String.format("%010d 00000 n \n", offset));
    }

    pdf.append("trailer\n");
    pdf.append("<< /Size ").append(objetos.size() + 1).append(" /Root 1 0 R >>\n");
    pdf.append("startxref\n");
    pdf.append(inicioXref).append("\n");
    pdf.append("%%EOF\n");

    return pdf.toString().getBytes(StandardCharsets.US_ASCII);
  }

  private String escaparPdf(String texto) {
    return texto.replace("\\", "\\\\")
        .replace("(", "\\(")
        .replace(")", "\\)");
  }

  private String normalizarAscii(String texto) {
    if (texto == null) {
      return "";
    }

    String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
    return normalizado.replaceAll("\\p{M}", "")
        .replaceAll("[^\\x20-\\x7E]", "");
  }

  private String textoSeguro(String texto) {
    return texto == null ? "" : texto;
  }

  private BigDecimal valorSeguro(BigDecimal valor) {
    return valor == null ? BigDecimal.ZERO : valor;
  }
}
