package cl.duoc.cordillera.reportservice.service.exportador;

import cl.duoc.cordillera.reportservice.model.Reporte;

public interface Exportador {

  byte[] exportar(Reporte reporte);

  String getContentType();

  String getExtension();
}
