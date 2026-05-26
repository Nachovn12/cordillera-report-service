# Report Service - Cordillera Platform

Microservicio backend de **Cordillera Platform**, correspondiente al Parcial 2 de la asignatura **Desarrollo Full Stack III (DSY1106)**.

## Descripcion

`report-service` es el microservicio encargado de gestionar reportes ejecutivos para Grupo Cordillera.

Permite:

- Crear reportes ejecutivos.
- Consultar reportes existentes.
- Filtrar reportes por area.
- Generar reportes a partir de KPIs.
- Exportar reportes en distintos formatos.
- Consumir `kpi-service` mediante Circuit Breaker con Resilience4j.

Este servicio es parte de una arquitectura de microservicios donde el frontend no lo consume directamente. El acceso desde la interfaz ocurre mediante el **BFF Gateway**.

## Rol dentro de la arquitectura

```text
Usuario
  -> Frontend React + Nginx :3000
  -> BFF Gateway :8081
  -> Report Service :8085
  -> MySQL Docker :3307 / report_db
```

## Stack utilizado

- Java 21
- Spring Boot
- Maven
- Spring Web
- Spring Data JPA
- MySQL
- Resilience4j
- JUnit 5
- Mockito
- JaCoCo
- Docker

## Puerto del servicio

| Servicio | Puerto |
|---|---:|
| Report Service | 8085 |

## Base de datos

| Motor | Base de datos |
|---|---|
| MySQL Docker | report_db |

En Docker Compose, el servicio se conecta a MySQL mediante:

```env
REPORT_DB_URL=jdbc:mysql://mysql:3306/report_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago
DB_USER=root
DB_PASSWORD=root
KPI_SERVICE_URL=http://kpi-service:8084
```

## Patron Repository

El servicio implementa Repository Pattern mediante:

```java
ReporteRepository extends JpaRepository<Reporte, Long>
```

Repositorio principal:

```text
src/main/java/cl/duoc/cordillera/reportservice/repository/ReporteRepository.java
```

Permite separar la persistencia de la logica de negocio y mantener el codigo desacoplado.

## Factory Method

El servicio implementa Factory Method para exportar reportes en distintos formatos.

Clases principales:

```text
ExportadorFactory
Exportador
PdfExportador
ExcelExportador
JsonExportador
```

Responsabilidad:

- `ExportadorFactory`: selecciona el exportador adecuado segun el formato solicitado.
- `PdfExportador`: genera contenido PDF.
- `ExcelExportador`: genera contenido Excel.
- `JsonExportador`: genera contenido JSON.
- `Exportador`: interfaz comun para todos los exportadores.

Este patron mejora la mantenibilidad porque permite agregar nuevos formatos sin modificar la logica central del servicio.

## Circuit Breaker

`report-service` consume `kpi-service` para generar reportes ejecutivos basados en KPIs.

La comunicacion esta protegida con Resilience4j mediante Circuit Breaker.

Flujo:

```text
Report Service -> KPI Service
```

Si `kpi-service` no responde, se ejecuta un fallback para evitar que la falla se propague al resto de la plataforma.

## Capas internas

```text
Controller -> Service -> Repository -> Model
```

Estructura principal:

```text
src/main/java/cl/duoc/cordillera/reportservice/
|-- controller
|-- service
|-- repository
|-- model
|-- dto
|-- factory
|-- exportador
`-- config
```

## Endpoints principales

Base URL local:

```text
http://localhost:8085
```

### Listar reportes

```http
GET /api/reportes
```

Ejemplo PowerShell:

```powershell
Invoke-RestMethod -Uri "http://localhost:8085/api/reportes" -Method Get | ConvertTo-Json -Depth 10
```

### Obtener reporte por ID

```http
GET /api/reportes/{id}
```

Ejemplo:

```powershell
Invoke-RestMethod -Uri "http://localhost:8085/api/reportes/1" -Method Get | ConvertTo-Json -Depth 10
```

### Crear reporte

```http
POST /api/reportes
```

Ejemplo:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8085/api/reportes" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{
    "titulo": "Reporte Ejecutivo Mayo 2026",
    "tipo": "PDF",
    "area": "Gerencia",
    "valor": 1250000
  }' | ConvertTo-Json -Depth 10
```

### Filtrar reportes por area

```http
GET /api/reportes/area/{area}
```

Ejemplo:

```powershell
Invoke-RestMethod -Uri "http://localhost:8085/api/reportes/area/Gerencia" -Method Get | ConvertTo-Json -Depth 10
```

### Generar reporte

```http
POST /api/reportes/generar
```

### Exportar reporte

```http
GET /api/reportes/{id}/exportar
```

Ejemplo:

```powershell
Invoke-WebRequest -Uri "http://localhost:8085/api/reportes/1/exportar" -OutFile "reporte-1.pdf"
```

### Eliminar reporte

```http
DELETE /api/reportes/{id}
```

## Ejecucion local

Desde la carpeta `report-service`:

```powershell
mvn spring-boot:run
```

Requiere una base MySQL disponible y variables de conexion configuradas.

## Ejecucion con Docker Compose

Desde la raiz del repositorio:

```powershell
docker compose up -d --build
```

Validar contenedores:

```powershell
docker compose ps
```

El servicio debe aparecer como:

```text
report-service   Up   0.0.0.0:8085->8085/tcp
```

## Validacion funcional realizada

Se valido el servicio mediante Docker Compose.

### Reporte creado

```json
{
  "id": 1,
  "titulo": "Reporte Ejecutivo Mayo 2026",
  "tipo": "PDF",
  "area": "Gerencia",
  "valor": 1250000,
  "fechaGeneracion": "2026-05-26T15:15:51.196017"
}
```

### Endpoint validado directamente

```powershell
Invoke-RestMethod -Uri "http://localhost:8085/api/reportes" -Method Get | ConvertTo-Json -Depth 10
```

Resultado:

```text
GET /api/reportes OK
Cantidad de reportes: 1
Reporte: Reporte Ejecutivo Mayo 2026
```

### Validacion via BFF Gateway

```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/reportes" -Method Get | ConvertTo-Json -Depth 10
```

Resultado:

```text
BFF Gateway entrega correctamente los reportes provenientes de report-service.
```

Tambien se valido:

```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/dashboard/stats" -Method Get | ConvertTo-Json -Depth 10
```

El campo `reportesRecientes` incluye el reporte `Reporte Ejecutivo Mayo 2026`.

## Evidencia de exportacion PDF

Desde el frontend se valido la descarga del archivo:

```text
reporte-1.pdf
```

Contenido generado:

```text
Reporte Ejecutivo Cordillera Platform
ID: 1
Titulo: Reporte Ejecutivo Mayo 2026
Tipo: PDF
Area: Gerencia
Valor: 1250000.00
Fecha Generacion: 2026-05-26T15:15:51.196017
```

## Pruebas unitarias

El servicio cuenta con pruebas unitarias usando:

- JUnit 5
- Mockito
- MockMvc
- JaCoCo

Comandos:

```powershell
cd report-service
mvn clean test
mvn clean verify
```

## Cobertura JaCoCo

Reporte generado en:

```text
report-service/target/site/jacoco/index.html
```

Cobertura obtenida:

```text
Instruction Coverage: 91%
Branch Coverage: 76%
```

Cumple el minimo requerido de cobertura igual o superior al 60%.

## Pruebas implementadas

Se validan:

- Logica de negocio de `ReporteService`.
- Endpoints principales de `ReporteController`.
- Exportacion mediante `ExportadorFactory`.
- Exportadores concretos:
  - `PdfExportador`
  - `ExcelExportador`
  - `JsonExportador`
- Fallback del cliente KPI asociado al Circuit Breaker.
- Configuracion de `RestTemplate`.
- Modelo `Reporte`.
- DTO `KpiResumenDto`.

## Estado actual

`report-service` queda funcional, documentado y validado para presentacion:

- Servicio operativo en Docker.
- Persistencia en `report_db`.
- Endpoint de reportes funcionando.
- Integracion via BFF Gateway validada.
- Reporte visible en frontend.
- Exportacion PDF funcionando.
- Pruebas unitarias y cobertura JaCoCo sobre el minimo requerido.
