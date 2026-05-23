package cl.duoc.cordillera.reportservice.service.exportador;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class ExportadorFactoryTest {

    private ExportadorFactory exportadorFactory;

    @BeforeEach
    void setUp() {
        exportadorFactory = new ExportadorFactory();
    }

    @Test
    void crearExportadorDebeRetornarPdf() {
        Exportador exportador = exportadorFactory.crearExportador("pdf");

        assertInstanceOf(PdfExportador.class, exportador);
    }

    @Test
    void crearExportadorDebeRetornarExcelParaExcel() {
        Exportador exportador = exportadorFactory.crearExportador("excel");

        assertInstanceOf(ExcelExportador.class, exportador);
    }

    @Test
    void crearExportadorDebeRetornarExcelParaXls() {
        Exportador exportador = exportadorFactory.crearExportador("xls");

        assertInstanceOf(ExcelExportador.class, exportador);
    }

    @Test
    void crearExportadorDebeRetornarJson() {
        Exportador exportador = exportadorFactory.crearExportador("json");

        assertInstanceOf(JsonExportador.class, exportador);
    }

    @Test
    void crearExportadorDebeLanzarBadRequestSiFormatoEsVacio() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exportadorFactory.crearExportador("")
        );

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void crearExportadorDebeLanzarBadRequestSiFormatoNoExiste() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exportadorFactory.crearExportador("xml")
        );

        assertEquals(400, ex.getStatusCode().value());
    }
}
