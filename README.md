# Report Service - Cordillera Platform

Microservicio responsable de gestionar, generar y exportar reportes ejecutivos para Cordillera Platform.

## 1. Descripción

`report-service` permite crear, consultar, actualizar, eliminar, generar y exportar reportes ejecutivos. Además, consulta KPIs desde `kpi-service` para enriquecer los reportes y utiliza `ExportadorFactory` para seleccionar el formato de exportación solicitado.

Este componente es prioritario para pruebas automatizadas dentro del proyecto.

## 2. Responsable

| Campo                 | Detalle                  |
| --------------------- | ------------------------ |
| Responsable principal | Ignacio Valeria          |
| Componente            | Report Service           |
| Rama sugerida         | `feature/report-service` |
| Puerto local          | `8085`                   |
| Base de datos         | `report_db`              |
| URL base local        | `http://localhost:8085`  |

## 3. Rol dentro de la arquitectura

```text
BFF Gateway -> Report Service -> KPI Service
                  |
                  v
              report_db
```

El frontend no consume este servicio directamente; el acceso desde la interfaz ocurre mediante el BFF Gateway.

## 4. Stack utilizado

- Java 21
- Spring Boot 4.0.6
- Maven
- Spring Web
- Spring Data JPA
- MySQL 8.4
- Resilience4j
- JUnit 5
- Mockito
- MockMvc
- JaCoCo
- Docker

## 5. Puerto y configuración

```properties
server.port=8085
spring.application.name=report-service

spring.datasource.url=${REPORT_DB_URL:jdbc:mysql://localhost:3307/report_db?createDatabaseIfNotExist=true}
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:root}

services.kpi.url=${KPI_SERVICE_URL:http://localhost:8084}
```

En Docker Compose, el servicio consume `kpi-service:8084` y se conecta a MySQL mediante `mysql:3306`.

## 6. Base de datos

| Campo             | Detalle          |
| ----------------- | ---------------- |
| Motor             | MySQL 8.4        |
| Host local Docker | `localhost:3307` |
| Puerto contenedor | `3306`           |
| Base lógica       | `report_db`      |
| Tabla principal   | `reportes`       |

## 7. Patrones y buenas prácticas aplicadas

| Patrón / práctica      | Aplicación                                         |
| ---------------------- | -------------------------------------------------- |
| Factory Method         | `ExportadorFactory` selecciona PDF, Excel o JSON.  |
| Strategy               | `Exportador` define contrato común de exportación. |
| Repository Pattern     | `ReporteRepository` extiende `JpaRepository`.      |
| Circuit Breaker        | Protege llamadas hacia KPI Service.                |
| Database per Service   | Usa base propia `report_db`.                       |
| Arquitectura por capas | Controller -> Service -> Repository -> Model.      |
| Pruebas automatizadas  | JUnit 5, Mockito, MockMvc y JaCoCo.                |

## 8. Clases principales

```text
ReporteController
ReporteService
ReporteRepository
Reporte
ExportadorFactory
Exportador
PdfExportador
ExcelExportador
JsonExportador
KpiClienteService
```

## 9. Modelo principal

Entidad `Reporte`:

```text
id
titulo
tipo
area
valor
fechaGeneracion
```

## 10. Endpoints principales

| Método | Endpoint                                   | Descripción                      |
| ------ | ------------------------------------------ | -------------------------------- |
| GET    | `/api/reportes`                            | Lista todos los reportes.        |
| POST   | `/api/reportes`                            | Crea un nuevo reporte.           |
| GET    | `/api/reportes/{id}`                       | Consulta un reporte por ID.      |
| PUT    | `/api/reportes/{id}`                       | Actualiza un reporte.            |
| DELETE | `/api/reportes/{id}`                       | Elimina un reporte.              |
| GET    | `/api/reportes/area/{area}`                | Filtra reportes por área.        |
| POST   | `/api/reportes/generar`                    | Genera un reporte ejecutivo.     |
| GET    | `/api/reportes/{id}/exportar`              | Exporta un reporte.              |
| GET    | `/api/reportes/kpis`                       | Consulta KPIs desde KPI Service. |
| GET    | `/api/reportes/kpis/categoria/{categoria}` | Consulta KPIs por categoría.     |

## 11. Exportación de reportes

Report Service utiliza `ExportadorFactory` para seleccionar el exportador adecuado:

```text
ExportadorFactory
|-- PdfExportador
|-- ExcelExportador
`-- JsonExportador
```

Formatos soportados:

```text
PDF
Excel
JSON
```

El PDF generado usa formato ejecutivo con encabezado, resumen, KPI principal, tabla de detalle, observación y footer institucional.

## 12. Circuit Breaker

Report Service consulta KPI Service para obtener indicadores.

```text
Report Service -> KPI Service
Circuit Breaker: kpiService
```

Si KPI Service no responde, se ejecuta un fallback para mantener disponible el servicio de reportes.

## 13. Ejecución local

```powershell
cd .\report-service\
.\mvnw.cmd spring-boot:run
```

## 14. Ejecución con Docker Compose

Desde la raíz del proyecto:

```powershell
docker compose up -d --build report-service
```

Para levantar toda la arquitectura:

```powershell
docker compose up -d --build
```

## 15. Pruebas

```powershell
cd .\report-service\
.\mvnw.cmd clean test
```

## 16. Cobertura con JaCoCo

```powershell
cd .\report-service\
.\mvnw.cmd clean test jacoco:report
```

Reporte generado:

```text
report-service/target/site/jacoco/index.html
```

Cobertura obtenida:

```text
Instruction Coverage: 91%
Branch Coverage: 76%
```

## 17. Pruebas manuales

```powershell
Invoke-RestMethod -Uri "http://localhost:8085/api/reportes" -Method Get
Invoke-RestMethod -Uri "http://localhost:8085/api/reportes/area/Gerencia" -Method Get
Invoke-RestMethod -Uri "http://localhost:8085/api/reportes/kpis" -Method Get
Invoke-WebRequest -Uri "http://localhost:8085/api/reportes/1/exportar" -OutFile "reporte-1.pdf"
```

## 18. Diagramas

### Diagrama de clases

![Diagrama de clases Report Service](../docs/diagramas/report-service-clases-ep3.png)

### Diagrama de casos de uso

![Diagrama de casos de uso Report Service](../docs/diagramas/report-service-casos-uso.png)

## 19. Historias de usuario y subtareas asociadas

| Código Jira | Tipo | Nombre | Responsable | Estado | Relación con Report Service |
|---|---|---|---|---|---|
| CORD-1 | Épica | EP-05 Report Service Cordillera Platform | Ignacio Valeria | Finalizada | Define el microservicio prioritario encargado de generar, consultar, filtrar y exportar reportes ejecutivos. |
| CORD-7 | Historia de usuario | HU-REPORT-01 CRUD de reportes | Ignacio Valeria | Finalizada | Implementa la gestión CRUD de reportes mediante `Reporte`, `ReporteRepository`, `ReporteService` y `ReporteController`. |
| CORD-10 | Historia de usuario | HU-REPORT-04 Circuit Breaker hacia KPI Service | Ignacio Valeria | Finalizada | Permite que Report Service siga respondiendo aunque KPI Service falle, evitando caída en cascada. |
| CORD-11 | Historia de usuario | HU-REPORT-05 Pruebas unitarias obligatorias | Ignacio Valeria | Finalizada | Exige pruebas unitarias, pruebas de controller, pruebas de exportadores, fallback y cobertura JaCoCo ≥ 60%. |
| CORD-12 | Historia de usuario | HU-REPORT-06 README y evidencia Report Service | Ignacio Valeria | Finalizada | Documenta Report Service, endpoints, Docker, pruebas, JaCoCo y evidencias técnicas. |
| CORD-34 | Subtarea | Crear entidad Reporte | Ignacio Valeria | Finalizada | Implementa entidad JPA `Reporte` con `id`, `titulo`, `tipo`, `area`, `valor` y `fechaGeneracion`. |
| CORD-40 | Subtarea | Implementar lógica de creación de reportes | Ignacio Valeria | Finalizada | Implementa lógica en `ReporteService` para construir y persistir reportes ejecutivos. |
| CORD-42 | Subtarea | Crear consulta de reportes por área | Ignacio Valeria | Finalizada | Implementa búsqueda por área en `ReporteService` y `ReporteController`. |
| CORD-38 | Subtarea | Probar CRUD de reportes | Ignacio Valeria | Finalizada | Valida manualmente el flujo CRUD de Report Service con datos de prueba en `report_db`. |

### Detalle funcional de las HU principales

**CORD-7 - HU-REPORT-01 CRUD de reportes**

Historia de usuario:

> Como gerente quiero crear y consultar reportes ejecutivos para revisar información estratégica del negocio.

Criterios de aceptación relacionados:

- Existe entidad `Reporte` con campos `id`, `titulo`, `tipo`, `area`, `valor` y `fechaGeneracion`.
- Existe `ReporteRepository` extendiendo `JpaRepository`.
- Existe `ReporteService` con lógica CRUD.
- Existe `ReporteController` exponiendo endpoints `/api/reportes`.
- Funcionan operaciones `GET`, `POST`, `PUT` y `DELETE`.
- El servicio corre en puerto `8085` y persiste en `report_db`.

**CORD-10 - HU-REPORT-04 Circuit Breaker hacia KPI Service**

Historia de usuario:

> Como sistema quiero que Report Service siga respondiendo aunque KPI Service falle para evitar caída en cascada.

Criterios de aceptación relacionados:

- Report Service consume KPI Service mediante REST.
- Se usa anotación `@CircuitBreaker` de Resilience4j en la capa Service.
- Existe `fallbackMethod` funcional.
- Si KPI Service falla, Report Service responde de forma degradada y controlada.
- Se documentan estados `CLOSED`, `OPEN` y `HALF-OPEN` para la defensa oral.

**CORD-11 - HU-REPORT-05 Pruebas unitarias obligatorias**

Historia de usuario:

> Como líder quiero asegurar cobertura mínima del Report Service para cumplir la rúbrica y defender calidad técnica.

Criterios de aceptación relacionados:

- Existe `ReportServiceTest` con JUnit 5 y Mockito.
- Existe `ReportControllerTest` con MockMvc.
- Existe prueba de `ExportadorFactory`.
- Existe prueba de fallback de Circuit Breaker.
- El comando `mvn test` pasa correctamente.
- JaCoCo reporta cobertura igual o superior al 60%.

**CORD-12 - HU-REPORT-06 README y evidencia Report Service**

Historia de usuario:

> Como líder quiero documentar Report Service para demostrar implementación, pruebas y cobertura en la entrega.

Criterios de aceptación relacionados:

- README incluye descripción, requisitos, Docker, endpoints y ejemplos.
- README incluye sección de pruebas unitarias.
- README incluye comandos `mvn test` y `jacoco:report`.
- Se guarda evidencia de endpoints funcionando.
- Se guarda evidencia de cobertura JaCoCo igual o superior al 60%.

Estas historias y subtareas permiten vincular la implementación técnica de Report Service con la planificación, seguimiento y evidencias del proyecto en Jira.

## 20. Evidencias relacionadas

- Servicio operativo en `http://localhost:8085`.
- Endpoint `/api/reportes` validado directamente.
- Reportes consumidos desde BFF Gateway mediante `/api/reportes`.
- Reporte `Reporte Ejecutivo Mayo 2026` visible en frontend.
- Exportación PDF funcionando mediante `/api/reportes/{id}/exportar`.
- Pruebas unitarias y cobertura JaCoCo sobre el mínimo requerido.
