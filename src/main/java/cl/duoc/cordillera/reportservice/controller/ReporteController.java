package cl.duoc.cordillera.reportservice.controller;

import cl.duoc.cordillera.reportservice.dto.KpiResumenDto;
import cl.duoc.cordillera.reportservice.model.Reporte;
import cl.duoc.cordillera.reportservice.service.ReporteService;
import cl.duoc.cordillera.reportservice.service.client.KpiClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Reportes", description = "Generación, consulta y exportación de reportes ejecutivos. Formatos: PDF, Excel, JSON.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final KpiClienteService kpiClienteService;

    private static final String EJEMPLO_REPORTE = """
            {
              "id": 1,
              "titulo": "Reporte Mensual Ventas Mayo",
              "tipo": "Mensual",
              "area": "Ventas",
              "valor": 380000.00,
              "fechaGeneracion": "2026-06-10T09:00:00"
            }""";

    private static final String EJEMPLO_REPORTE_LISTA = """
            [
              {
                "id": 1,
                "titulo": "Reporte Mensual Ventas Mayo",
                "tipo": "Mensual",
                "area": "Ventas",
                "valor": 380000.00,
                "fechaGeneracion": "2026-06-10T09:00:00"
              },
              {
                "id": 2,
                "titulo": "Reporte Logística Q2",
                "tipo": "Trimestral",
                "area": "Logística",
                "valor": 94200.00,
                "fechaGeneracion": "2026-06-10T09:30:00"
              }
            ]""";

    private static final String EJEMPLO_REPORTE_REQUEST = """
            {
              "titulo": "Reporte Mensual Ventas Mayo",
              "tipo": "Mensual",
              "area": "Ventas",
              "valor": 380000.00
            }""";

    private static final String EJEMPLO_KPI_LISTA = """
            [
              {
                "id": 1,
                "nombre": "Ventas Totales",
                "valor": 380000.00,
                "unidad": "CLP",
                "categoria": "Ventas",
                "estado": "Activo"
              },
              {
                "id": 2,
                "nombre": "Nivel de Stock",
                "valor": 87.50,
                "unidad": "%",
                "categoria": "Inventario",
                "estado": "Activo"
              }
            ]""";

    private static final String EJEMPLO_404 = """
            {
              "status": 404,
              "error": "Not Found",
              "message": "Reporte con id 99 no encontrado"
            }""";

    private static final String EJEMPLO_400 = """
            {
              "status": 400,
              "error": "Bad Request",
              "message": "El título del reporte es obligatorio"
            }""";

    @Operation(summary = "Listar todos los reportes")
    @ApiResponse(responseCode = "200", description = "Lista de reportes",
        content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = Reporte.class)),
            examples = @ExampleObject(name = "Lista de reportes", value = EJEMPLO_REPORTE_LISTA)))
    @GetMapping
    public ResponseEntity<List<Reporte>> listarTodos() {
        return ResponseEntity.ok(reporteService.listarTodos());
    }

    @Operation(summary = "Listar reportes por área")
    @ApiResponse(responseCode = "200", description = "Reportes del área",
        content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = Reporte.class)),
            examples = @ExampleObject(name = "Reportes área Ventas", value = """
                    [
                      {
                        "id": 1,
                        "titulo": "Reporte Mensual Ventas Mayo",
                        "tipo": "Mensual",
                        "area": "Ventas",
                        "valor": 380000.00,
                        "fechaGeneracion": "2026-06-10T09:00:00"
                      }
                    ]""")))
    @GetMapping("/area/{area}")
    public ResponseEntity<List<Reporte>> listarPorArea(
            @Parameter(description = "Área del reporte (p.ej. Ventas, Logística)", required = true, example = "Ventas")
            @PathVariable String area) {
        return ResponseEntity.ok(reporteService.listarPorArea(area));
    }

    @Operation(summary = "Listar KPIs desde kpi-service", description = "Consulta remota a kpi-service con circuit breaker Resilience4j.")
    @ApiResponse(responseCode = "200", description = "Lista de KPIs",
        content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = KpiResumenDto.class)),
            examples = @ExampleObject(name = "KPIs desde kpi-service", value = EJEMPLO_KPI_LISTA)))
    @GetMapping("/kpis")
    public ResponseEntity<List<KpiResumenDto>> listarKpis() {
        return ResponseEntity.ok(kpiClienteService.obtenerKpis());
    }

    @Operation(summary = "KPIs por categoría desde kpi-service")
    @ApiResponse(responseCode = "200", description = "KPIs de la categoría",
        content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = KpiResumenDto.class)),
            examples = @ExampleObject(name = "KPIs Ventas", value = """
                    [
                      {
                        "id": 1,
                        "nombre": "Ventas Totales",
                        "valor": 380000.00,
                        "unidad": "CLP",
                        "categoria": "Ventas",
                        "estado": "Activo"
                      }
                    ]""")))
    @GetMapping("/kpis/categoria/{categoria}")
    public ResponseEntity<List<KpiResumenDto>> listarKpisPorCategoria(
            @Parameter(description = "Categoría", required = true, example = "Ventas") @PathVariable String categoria) {
        return ResponseEntity.ok(kpiClienteService.obtenerKpisPorCategoria(categoria));
    }

    @Operation(summary = "Exportar reporte", description = "Descarga el reporte en PDF (default), Excel o JSON.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Archivo generado",
            content = @Content(mediaType = "application/octet-stream")),
        @ApiResponse(responseCode = "404", description = "Reporte no encontrado",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "No encontrado", value = EJEMPLO_404)))
    })
    @GetMapping("/{id}/exportar")
    public ResponseEntity<byte[]> exportar(
            @Parameter(description = "ID del reporte", required = true, example = "1") @PathVariable Long id,
            @Parameter(description = "Formato: pdf (default), excel, json", example = "pdf")
            @RequestParam(defaultValue = "pdf") String formato) {
        byte[] contenido = reporteService.exportar(id, formato);

        String extension = switch (formato.toLowerCase()) {
            case "excel", "xls", "xlsx" -> "xls";
            case "json" -> "json";
            default -> "pdf";
        };

        String contentType = switch (formato.toLowerCase()) {
            case "excel", "xls", "xlsx" -> "application/vnd.ms-excel";
            case "json" -> "application/json";
            default -> "application/pdf";
        };

        String nombreArchivo = "reporte-" + id + "." + extension;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .body(contenido);
    }

    @Operation(summary = "Buscar reporte por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reporte encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Reporte.class),
                examples = @ExampleObject(name = "Reporte encontrado", value = EJEMPLO_REPORTE))),
        @ApiResponse(responseCode = "404", description = "Reporte no encontrado",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "No encontrado", value = EJEMPLO_404)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Reporte> buscarPorId(
            @Parameter(description = "ID del reporte", required = true, example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(reporteService.buscarPorId(id));
    }

    @Operation(summary = "Crear reporte")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Reporte creado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Reporte.class),
                examples = @ExampleObject(name = "Reporte creado", value = EJEMPLO_REPORTE))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "Error validación", value = EJEMPLO_400)))
    })
    @PostMapping
    public ResponseEntity<Reporte> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Campos requeridos: titulo, tipo, area, valor",
                required = true,
                content = @Content(schema = @Schema(implementation = Reporte.class),
                    examples = @ExampleObject(name = "Nuevo reporte", value = EJEMPLO_REPORTE_REQUEST)))
            @Valid @RequestBody Reporte reporte) {
        // Fix EP3 (cambio 2 del profesor): delega en generarReporte() para que aplique
        // la regla de unicidad (area, tipo, anio, mes). Asi se evita el caso reportado
        // por el profesor: "se generaba 2 veces el mismo reporte de los meses".
        Reporte reporteResultado = reporteService.generarReporte(reporte);
        if (reporteResultado.getId() != null && !reporteResultado.getId().equals(reporte.getId())) {
            // Se devuelve un reporte ya existente -> 200 OK
            return ResponseEntity.ok(reporteResultado);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteResultado);
    }

    @Operation(summary = "Generar reporte ejecutivo", description = "Genera un reporte completo a partir de los datos enviados. Puede incluir KPIs y datos de exportación.")
    @ApiResponse(responseCode = "201", description = "Reporte generado",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Reporte.class),
            examples = @ExampleObject(name = "Reporte generado", value = EJEMPLO_REPORTE)))
    @PostMapping("/generar")
    public ResponseEntity<Reporte> generar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                content = @Content(examples = @ExampleObject(name = "Datos para generar", value = EJEMPLO_REPORTE_REQUEST)))
            @RequestBody Reporte reporte) {
        Reporte reporteGenerado = reporteService.generarReporte(reporte);
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteGenerado);
    }

    @Operation(summary = "Actualizar reporte")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reporte actualizado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Reporte.class),
                examples = @ExampleObject(name = "Reporte actualizado", value = EJEMPLO_REPORTE))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "Error validación", value = EJEMPLO_400))),
        @ApiResponse(responseCode = "404", description = "Reporte no encontrado",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "No encontrado", value = EJEMPLO_404)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Reporte> actualizar(
            @Parameter(description = "ID del reporte", required = true, example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                content = @Content(examples = @ExampleObject(name = "Datos actualizados", value = EJEMPLO_REPORTE_REQUEST)))
            @Valid @RequestBody Reporte reporte) {
        return ResponseEntity.ok(reporteService.actualizar(id, reporte));
    }

    @Operation(summary = "Eliminar reporte")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reporte eliminado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Reporte no encontrado",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "No encontrado", value = EJEMPLO_404)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del reporte", required = true, example = "1") @PathVariable Long id) {
        reporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}