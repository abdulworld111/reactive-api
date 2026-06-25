package com.api.reactive;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.api.reactive.dto.request.ProductRequest;
import com.api.reactive.dto.response.ProductResponse;
import com.api.reactive.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = com.api.reactive.controller.ProductController.class)
@Import(com.api.reactive.exception.GlobalExceptionHandler.class)
class ProductControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("POST /api/v1/products — 201 Created")
    void createProduct_returns201() {
        ProductRequest req = ProductRequest.builder()
                .name("Keyboard").price(BigDecimal.valueOf(129.99))
                .stock(50).category("Electronics").sku("ELEC-KBD-001").active(true)
                .build();

        ProductResponse res = ProductResponse.builder()
                .id(1L).name("Keyboard").price(BigDecimal.valueOf(129.99))
                .stock(50).category("Electronics").sku("ELEC-KBD-001").active(true)
                .build();

        when(productService.createProduct(any())).thenReturn(Mono.just(res));

        webTestClient.post().uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponse.class)
                .value(r -> {
                    assertEquals(1L, r.getId());
                    assertEquals("ELEC-KBD-001", r.getSku());
                });
    }

    @Test
    @DisplayName("POST /api/v1/products — 400 when name blank")
    void createProduct_invalidInput_returns400() {
        ProductRequest invalid = ProductRequest.builder()
                .name("").price(BigDecimal.valueOf(10)).stock(1)
                .category("Cat").sku("VALID-001").build();

        webTestClient.post().uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalid)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} — 200 OK")
    void getProductById_returns200() {
        ProductResponse res = ProductResponse.builder()
                .id(1L).name("Test").sku("TST-001").build();

        when(productService.getProductById(1L)).thenReturn(Mono.just(res));

        webTestClient.get().uri("/api/v1/products/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class)
                .value(r -> assertEquals(1L, r.getId()));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} — 204 No Content")
    void deleteProduct_returns204() {
        when(productService.deleteProduct(1L)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/products/1")
                .exchange()
                .expectStatus().isNoContent();
    }
}
