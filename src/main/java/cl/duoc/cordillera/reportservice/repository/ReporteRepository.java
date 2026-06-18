package cl.duoc.cordillera.reportservice.repository;

import cl.duoc.cordillera.reportservice.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Patrón Repository: abstrae el acceso a datos de Reporte.
 * Spring Data JPA genera la implementación en tiempo de ejecución.
 * Métodos de consulta siguen convención Spring Data (findBy...).
 */
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

  List<Reporte> findByArea(String area);

  /**
   * Busca un reporte por la combinacion unica (area, tipo, anio, mes).
   * Usado para evitar generar el mismo reporte dos veces (EP3 - cambio 2 profesor).
   */
  Optional<Reporte> findByAreaAndTipoAndAnioAndMes(
      String area, String tipo, Integer anio, Integer mes);
}
