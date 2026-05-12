package cl.duoc.cordillera.reportservice.service;

import cl.duoc.cordillera.reportservice.model.Reporte;
import cl.duoc.cordillera.reportservice.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReporteService {

  private final ReporteRepository reporteRepository;

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
  public List<Reporte> listarPorArea(String area) {
    return reporteRepository.findByArea(area);
  }
}
