package cl.duoc.cordillera.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiResumenDto {

    private Long id;
    private String nombre;
    private BigDecimal valor;
    private String unidad;
    private String categoria;
    private String estado;
}