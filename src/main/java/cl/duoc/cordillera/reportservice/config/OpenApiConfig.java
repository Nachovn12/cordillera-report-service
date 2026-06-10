package cl.duoc.cordillera.reportservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title       = "Report Service — Cordillera Platform",
        version     = "1.0.0",
        description = """
            Microservicio de reportes ejecutivos.
            Genera y exporta reportes en PDF, Excel y JSON.
            Consulta KPIs desde kpi-service con circuit breaker Resilience4j.
            Persistencia en MySQL — base de datos: report_db.
            """,
        contact = @Contact(
            name  = "Equipo Cordillera",
            email = "dev@duoc.cl"
        ),
        license = @License(name = "MIT")
    ),
    servers = {
        @Server(url = "http://localhost:8085",    description = "Desarrollo local"),
        @Server(url = "http://report-service:8085", description = "Docker Compose")
    }
)
@Configuration
public class OpenApiConfig {
}
