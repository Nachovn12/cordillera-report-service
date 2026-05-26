package cl.duoc.cordillera.reportservice.service.exportador;

import cl.duoc.cordillera.reportservice.model.Reporte;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class PdfExportador implements Exportador {

  private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
  private static final DecimalFormat MONTO_FORMATO = crearFormatoMonto();

  @Override
  public byte[] exportar(Reporte reporte) {
    return crearPdfEjecutivo(reporte);
  }

  @Override
  public String getContentType() {
    return "application/pdf";
  }

  @Override
  public String getExtension() {
    return "pdf";
  }

  private byte[] crearPdfEjecutivo(Reporte reporte) {
    StringBuilder stream = new StringBuilder();

    String titulo = textoSeguro(reporte.getTitulo());
    String tipo = textoSeguro(reporte.getTipo());
    String area = textoSeguro(reporte.getArea());
    String valor = formatearClp(reporte.getValor());
    String fecha = formatearFecha(reporte.getFechaGeneracion());
    String id = String.valueOf(reporte.getId());

    // Header ejecutivo.
    rect(stream, 0, 706, 612, 86, "0.04 0.15 0.27");
    rect(stream, 0, 706, 612, 4, "0.00 0.62 0.58");
    texto(stream, "CORDILLERA Platform", 46, 758, 18, "F2", "1 1 1");
    texto(stream, "Reporte Ejecutivo", 46, 734, 11, "F1", "0.78 0.88 0.96");
    texto(stream, "Fecha de generacion: " + fecha, 396, 742, 10, "F1", "0.78 0.88 0.96");

    // Titulo principal.
    texto(stream, "Reporte Ejecutivo Cordillera Platform", 46, 664, 22, "F2", "0.05 0.09 0.17");
    texto(stream, "Vista consolidada para apoyar decisiones ejecutivas de Grupo Cordillera.", 46, 642, 10,
        "F1", "0.32 0.39 0.50");

    // Resumen ejecutivo.
    rect(stream, 42, 492, 528, 122, "0.95 0.97 0.99");
    rectLinea(stream, 42, 492, 528, 122, "0.82 0.87 0.92");
    rect(stream, 42, 602, 528, 12, "0.00 0.62 0.58");
    texto(stream, "Resumen ejecutivo", 62, 578, 14, "F2", "0.05 0.09 0.17");
    etiquetaValor(stream, "Titulo del reporte", titulo, 62, 550);
    etiquetaValor(stream, "Area", area, 62, 520);
    etiquetaValor(stream, "Tipo", tipo, 324, 550);
    etiquetaValor(stream, "Fecha de generacion", fecha, 324, 520);

    // KPI principal.
    rect(stream, 42, 374, 528, 88, "1 1 1");
    rectLinea(stream, 42, 374, 528, 88, "0.82 0.87 0.92");
    rect(stream, 42, 374, 8, 88, "0.00 0.62 0.58");
    texto(stream, "KPI principal", 66, 432, 12, "F2", "0.32 0.39 0.50");
    texto(stream, valor, 66, 404, 27, "F2", "0.00 0.48 0.45");
    texto(stream, "Valor consolidado del reporte ejecutivo", 66, 386, 10, "F1", "0.42 0.48 0.58");

    // Tabla de detalle.
    texto(stream, "Detalle del reporte", 42, 336, 14, "F2", "0.05 0.09 0.17");
    rect(stream, 42, 298, 528, 24, "0.04 0.15 0.27");
    rect(stream, 42, 244, 528, 54, "0.98 0.99 1");
    rectLinea(stream, 42, 244, 528, 78, "0.82 0.87 0.92");

    texto(stream, "ID", 54, 306, 9, "F2", "1 1 1");
    texto(stream, "Titulo", 88, 306, 9, "F2", "1 1 1");
    texto(stream, "Tipo", 258, 306, 9, "F2", "1 1 1");
    texto(stream, "Area", 332, 306, 9, "F2", "1 1 1");
    texto(stream, "Valor", 410, 306, 9, "F2", "1 1 1");
    texto(stream, "Fecha generacion", 492, 306, 9, "F2", "1 1 1");

    texto(stream, id, 54, 272, 9, "F1", "0.10 0.16 0.25");
    texto(stream, recortar(titulo, 28), 88, 272, 9, "F1", "0.10 0.16 0.25");
    texto(stream, recortar(tipo, 12), 258, 272, 9, "F1", "0.10 0.16 0.25");
    texto(stream, recortar(area, 12), 332, 272, 9, "F1", "0.10 0.16 0.25");
    texto(stream, valor.replace(" CLP", ""), 410, 272, 9, "F1", "0.10 0.16 0.25");
    texto(stream, fecha, 492, 272, 9, "F1", "0.10 0.16 0.25");

    // Observacion.
    rect(stream, 42, 154, 528, 58, "0.93 0.98 0.97");
    rectLinea(stream, 42, 154, 528, 58, "0.68 0.90 0.86");
    texto(stream, "Observacion", 62, 190, 12, "F2", "0.00 0.48 0.45");
    texto(stream, "Reporte generado automaticamente por Report Service para apoyar la toma de decisiones", 62, 174, 10,
        "F1", "0.16 0.24 0.35");
    texto(stream, "ejecutivas de Grupo Cordillera.", 62, 160, 10, "F1", "0.16 0.24 0.35");

    // Footer.
    linea(stream, 42, 92, 570, 92, "0.82 0.87 0.92");
    texto(stream, "Cordillera Platform - Report Service - Documento generado automaticamente", 112, 70, 9, "F1",
        "0.42 0.48 0.58");

    String streamTexto = stream.toString();

    java.util.List<String> objetos = java.util.List.of(
        "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
        "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n",
        "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> >>\nendobj\n",
        "4 0 obj\n<< /Length " + streamTexto.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n" + streamTexto
            + "endstream\nendobj\n",
        "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n",
        "6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

    StringBuilder pdf = new StringBuilder();
    java.util.List<Integer> offsets = new ArrayList<>();

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

  private void etiquetaValor(StringBuilder stream, String etiqueta, String valor, int x, int y) {
    texto(stream, etiqueta, x, y + 14, 8, "F2", "0.42 0.48 0.58");
    texto(stream, recortar(valor, 30), x, y, 11, "F1", "0.05 0.09 0.17");
  }

  private void texto(StringBuilder stream, String texto, int x, int y, int size, String fuente, String color) {
    stream.append("BT\n")
        .append(color).append(" rg\n")
        .append("/").append(fuente).append(" ").append(size).append(" Tf\n")
        .append(x).append(" ").append(y).append(" Td\n")
        .append("(").append(escaparPdf(normalizarAscii(texto))).append(") Tj\n")
        .append("ET\n");
  }

  private void rect(StringBuilder stream, int x, int y, int ancho, int alto, String color) {
    stream.append("q\n")
        .append(color).append(" rg\n")
        .append(x).append(" ").append(y).append(" ").append(ancho).append(" ").append(alto).append(" re f\n")
        .append("Q\n");
  }

  private void rectLinea(StringBuilder stream, int x, int y, int ancho, int alto, String color) {
    stream.append("q\n")
        .append(color).append(" RG\n")
        .append("1 w\n")
        .append(x).append(" ").append(y).append(" ").append(ancho).append(" ").append(alto).append(" re S\n")
        .append("Q\n");
  }

  private void linea(StringBuilder stream, int x1, int y1, int x2, int y2, String color) {
    stream.append("q\n")
        .append(color).append(" RG\n")
        .append("1 w\n")
        .append(x1).append(" ").append(y1).append(" m\n")
        .append(x2).append(" ").append(y2).append(" l S\n")
        .append("Q\n");
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

  private String formatearClp(BigDecimal valor) {
    BigDecimal valorSeguro = valor == null ? BigDecimal.ZERO : valor;
    return "$" + MONTO_FORMATO.format(valorSeguro.setScale(0, RoundingMode.HALF_UP)) + " CLP";
  }

  private String formatearFecha(LocalDateTime fecha) {
    return fecha == null ? "Sin fecha" : fecha.format(FECHA_FORMATO);
  }

  private String recortar(String texto, int maximo) {
    String seguro = textoSeguro(texto);
    if (seguro.length() <= maximo) {
      return seguro;
    }

    return seguro.substring(0, Math.max(0, maximo - 3)) + "...";
  }

  private static DecimalFormat crearFormatoMonto() {
    DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.forLanguageTag("es-CL"));
    simbolos.setGroupingSeparator('.');
    DecimalFormat formato = new DecimalFormat("#,##0", simbolos);
    formato.setParseBigDecimal(true);
    return formato;
  }
}
