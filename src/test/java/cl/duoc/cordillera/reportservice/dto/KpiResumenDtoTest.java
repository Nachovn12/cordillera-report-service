package cl.duoc.cordillera.reportservice.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class KpiResumenDtoTest {

  @Test
  void builderDebeCrearDtoCorrectamente() {
    KpiResumenDto dto = KpiResumenDto.builder()
        .id(1L)
        .nombre("Ventas Totales")
        .valor(BigDecimal.valueOf(380000))
        .unidad("CLP")
        .categoria("ventas")
        .estado("OPERATIVO")
        .build();

    assertEquals(1L, dto.getId());
    assertEquals("Ventas Totales", dto.getNombre());
    assertEquals(BigDecimal.valueOf(380000), dto.getValor());
    assertEquals("CLP", dto.getUnidad());
    assertEquals("ventas", dto.getCategoria());
    assertEquals("OPERATIVO", dto.getEstado());
  }

  @Test
  void settersYGettersDebenFuncionar() {
    KpiResumenDto dto = new KpiResumenDto();

    dto.setId(2L);
    dto.setNombre("Stock Disponible");
    dto.setValor(BigDecimal.valueOf(820));
    dto.setUnidad("unidades");
    dto.setCategoria("inventario");
    dto.setEstado("OPERATIVO");

    assertEquals(2L, dto.getId());
    assertEquals("Stock Disponible", dto.getNombre());
    assertEquals(BigDecimal.valueOf(820), dto.getValor());
    assertEquals("unidades", dto.getUnidad());
    assertEquals("inventario", dto.getCategoria());
    assertEquals("OPERATIVO", dto.getEstado());
  }

  @Test
  void constructorCompletoDebeAsignarCampos() {
    KpiResumenDto dto = new KpiResumenDto(
        3L,
        "Rentabilidad",
        BigDecimal.valueOf(23.5),
        "porcentaje",
        "rentabilidad",
        "OPERATIVO");

    assertEquals(3L, dto.getId());
    assertEquals("Rentabilidad", dto.getNombre());
    assertEquals(BigDecimal.valueOf(23.5), dto.getValor());
    assertEquals("porcentaje", dto.getUnidad());
    assertEquals("rentabilidad", dto.getCategoria());
    assertEquals("OPERATIVO", dto.getEstado());
  }
}
