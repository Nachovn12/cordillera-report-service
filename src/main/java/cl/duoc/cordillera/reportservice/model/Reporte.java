package cl.duoc.cordillera.reportservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "reportes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_reporte_periodo",
            columnNames = {"area", "tipo", "anio", "mes"}
        )
    },
    indexes = {
        @jakarta.persistence.Index(name = "idx_reporte_anio", columnList = "anio"),
        @jakarta.persistence.Index(name = "idx_reporte_area_anio", columnList = "area, anio")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reporte {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "El título del reporte es obligatorio")
  @Column(nullable = false, length = 150)
  private String titulo;

  @NotBlank(message = "El tipo de reporte es obligatorio")
  @Column(nullable = false, length = 50)
  private String tipo;

  @NotBlank(message = "El área del reporte es obligatoria")
  @Column(nullable = false, length = 80)
  private String area;

  @NotNull(message = "El valor del reporte es obligatorio")
  @PositiveOrZero(message = "El valor del reporte no puede ser negativo")
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal valor;

  @Column(nullable = false)
  private LocalDateTime fechaGeneracion;

  @Column(name = "anio")
  private Integer anio;

  @Column(name = "mes")
  private Integer mes;

  @PrePersist
  public void prePersist() {
    if (this.fechaGeneracion == null) {
      this.fechaGeneracion = LocalDateTime.now();
    }
    if (this.anio == null && this.fechaGeneracion != null) {
      this.anio = this.fechaGeneracion.getYear();
    }
    if (this.mes == null && this.fechaGeneracion != null) {
      this.mes = this.fechaGeneracion.getMonthValue();
    }
  }
}
