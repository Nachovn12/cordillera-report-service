package cl.duoc.cordillera.reportservice.service;

import cl.duoc.cordillera.reportservice.model.Reporte;
import cl.duoc.cordillera.reportservice.repository.ReporteRepository;
import cl.duoc.cordillera.reportservice.service.exportador.Exportador;
import cl.duoc.cordillera.reportservice.service.exportador.ExportadorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReporteService {

  private final ReporteRepository reporteRepository;
  private final ExportadorFactory exportadorFactory;

  @Transactional(readOnly = true)
  public List<Reporte> listarTodos() {
    return reporteRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Reporte buscarPorId(Long id) {
    return reporteRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Reporte no encontrado con id: " + id));
  }

  public Reporte crear(Reporte reporte) {
    reporte.setId(null);
    return reporteRepository.save(reporte);
  }

  public Reporte generarReporte(Reporte reporte) {
    validarDatosGeneracion(reporte);

    reporte.setId(null);

    if (!StringUtils.hasText(reporte.getTitulo())) {
      reporte.setTitulo("Reporte ejecutivo de " + reporte.getArea());
    }

    if (!StringUtils.hasText(reporte.getTipo())) {
      reporte.setTipo("EJECUTIVO");
    }

    if (reporte.getFechaGeneracion() == null) {
      reporte.setFechaGeneracion(LocalDateTime.now());
    }

    return reporteRepository.save(reporte);
  }

  public Reporte actualizar(Long id, Reporte reporte) {
    Reporte existente = buscarPorId(id);

    existente.setTitulo(reporte.getTitulo());
    existente.setTipo(reporte.getTipo());
    existente.setArea(reporte.getArea());
    existente.setValor(reporte.getValor());

    if (reporte.getFechaGeneracion() != null) {
      existente.setFechaGeneracion(reporte.getFechaGeneracion());
    }

    return reporteRepository.save(existente);
  }

  public void eliminar(Long id) {
    Reporte existente = buscarPorId(id);
    reporteRepository.delete(existente);
  }

  @Transactional(readOnly = true)
  public byte[] exportar(Long id, String formato) {
    Reporte reporte = buscarPorId(id);
    Exportador exportador = exportadorFactory.crearExportador(formato);
    return exportador.exportar(reporte);
  }

  @Transactional(readOnly = true)
  public List<Reporte> listarPorArea(String area) {
    return reporteRepository.findByArea(area);
  }

  private void validarDatosGeneracion(Reporte reporte) {
    if (reporte == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Los datos del reporte son obligatorios");
    }

    if (!StringUtils.hasText(reporte.getArea())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "El área del reporte es obligatoria");
    }

    if (reporte.getValor() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "El valor del reporte es obligatorio");
    }

    if (reporte.getValor().compareTo(BigDecimal.ZERO) < 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "El valor del reporte no puede ser negativo");
    }
  }
}
