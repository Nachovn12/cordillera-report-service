package cl.duoc.cordillera.reportservice.config;

import cl.duoc.cordillera.reportservice.model.Reporte;
import cl.duoc.cordillera.reportservice.repository.ReporteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private final ReporteRepository reporteRepository;

    public DataLoader(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    @Override
    public void run(String... args) {
        if (reporteRepository.count() == 0) {
            Reporte rep1 = new Reporte();
            rep1.setTitulo("Reporte Consolidado Semestral Q2");
            rep1.setTipo("CONSOLIDADO");
            rep1.setArea("DIRECCION");
            rep1.setValor(new BigDecimal("12500000.00"));
            rep1.setFechaGeneracion(LocalDateTime.now().minusDays(5));

            Reporte rep2 = new Reporte();
            rep2.setTitulo("Desempeno E-commerce vs POS");
            rep2.setTipo("COMPARATIVO");
            rep2.setArea("VENTAS");
            rep2.setValor(new BigDecimal("4580000.00"));
            rep2.setFechaGeneracion(LocalDateTime.now().minusDays(2));

            Reporte rep3 = new Reporte();
            rep3.setTitulo("Analisis de Quiebre de Stock");
            rep3.setTipo("ANALITICO");
            rep3.setArea("OPERACIONES");
            rep3.setValor(new BigDecimal("12"));
            rep3.setFechaGeneracion(LocalDateTime.now().minusDays(1));

            Reporte rep4 = new Reporte();
            rep4.setTitulo("Estado de Resultados Mensual");
            rep4.setTipo("FINANCIERO");
            rep4.setArea("FINANZAS");
            rep4.setValor(new BigDecimal("35000000.00"));
            rep4.setFechaGeneracion(LocalDateTime.now().minusHours(10));

            reporteRepository.save(rep1);
            reporteRepository.save(rep2);
            reporteRepository.save(rep3);
            reporteRepository.save(rep4);
        }
    }
}
