package cl.duoc.cordillera.reportservice.repository;

import cl.duoc.cordillera.reportservice.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {

  List<Reporte> findByArea(String area);
}
