package cl.duoc.cordillera.reportservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestTemplateConfigTest {

  @Test
  void restTemplateDebeCrearseCorrectamente() {
    RestTemplateConfig config = new RestTemplateConfig();

    RestTemplate restTemplate = config.restTemplate();

    assertNotNull(restTemplate);
  }
}
