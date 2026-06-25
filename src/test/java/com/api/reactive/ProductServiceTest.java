package com.api.reactive;

import com.api.reactive.dto.request.ProductRequest;
import com.api.reactive.dto.response.ProductResponse;
import com.api.reactive.entity.Product;
import com.api.reactive.exception.DuplicateResourceException;
import com.api.reactive.exception.ResourceNotFoundException;
import com.api.reactive.mapper.ProductMapper;
import com.api.reactive.repository.ProductRepository;
import com.api.reactive.repository.ProductRepositoryCustom;
import com.api.reactive.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository       productRepository;
    @Mock private ProductRepositoryCustom productRepositoryCustom;
    @Mock private ProductMapper           productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product        sampleProduct;
    private ProductRequest sampleRequest;
    private ProductResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L).name("Test Product").price(BigDecimal.valueOf(99.99))
                .stock(10).category("Electronics").sku("ELEC-TEST-001")
                .active(true).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();

        sampleRequest = ProductRequest.builder()
                .name("Test Product").price(BigDecimal.valueOf(99.99))
                .stock(10).category("Electronics").sku("ELEC-TEST-001").active(true)
                .build();

        sampleResponse = ProductResponse.builder()
                .id(1L).name("Test Product").price(BigDecimal.valueOf(99.99))
                .stock(10).category("Electronics").sku("ELEC-TEST-001").active(true)
                .build();
    }

    @Test
    @DisplayName("createProduct — happy path — returns saved product")
    void createProduct_success() {
        when(productRepository.existsBySku(anyString())).thenReturn(Mono.just(false));
        when(productMapper.toEntity(any())).thenReturn(sampleProduct);
        when(productRepository.save(any())).thenReturn(Mono.just(sampleProduct));
        when(productMapper.toResponse(any())).thenReturn(sampleResponse);

        StepVerifier.create(productService.createProduct(sampleRequest))
                .expectNextMatches(r -> "ELEC-TEST-001".equals(r.getSku()))
                .verifyComplete();
    }

    @Test
    @DisplayName("createProduct — duplicate SKU — emits DuplicateResourceException")
    void createProduct_duplicateSku() {
        when(productRepository.existsBySku(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(productService.createProduct(sampleRequest))
                .expectError(DuplicateResourceException.class)
                .verify();
    }

    @Test
    @DisplayName("getProductById — found — returns product")
    void getProductById_found() {
        when(productRepository.findById(1L)).thenReturn(Mono.just(sampleProduct));
        when(productMapper.toResponse(any())).thenReturn(sampleResponse);

        StepVerifier.create(productService.getProductById(1L))
                .expectNextMatches(r -> r.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    @DisplayName("getProductById — not found — emits ResourceNotFoundException")
    void getProductById_notFound() {
        when(productRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(productService.getProductById(99L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("deleteProduct — found — completes without error")
    void deleteProduct_success() {
        when(productRepository.findById(1L)).thenReturn(Mono.just(sampleProduct));
        when(productRepository.delete(any())).thenReturn(Mono.empty());

        StepVerifier.create(productService.deleteProduct(1L))
                .verifyComplete();
    }

    @Test
    @DisplayName("deleteProduct — not found — emits ResourceNotFoundException")
    void deleteProduct_notFound() {
        when(productRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(productService.deleteProduct(99L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("toggleProductStatus — flips active flag")
    void toggleStatus_flipsFlag() {
        sampleProduct.setActive(true);
        Product toggled = Product.builder()
                .id(1L).name("Test Product").price(BigDecimal.valueOf(99.99))
                .stock(10).category("Electronics").sku("ELEC-TEST-001")
                .active(false).createdAt(sampleProduct.getCreatedAt()).updatedAt(OffsetDateTime.now())
                .build();
        ProductResponse toggledResponse = ProductResponse.builder()
                .id(1L).active(false).build();

        when(productRepository.findById(1L)).thenReturn(Mono.just(sampleProduct));
        when(productRepository.save(any())).thenReturn(Mono.just(toggled));
        when(productMapper.toResponse(any())).thenReturn(toggledResponse);

        StepVerifier.create(productService.toggleProductStatus(1L))
                .expectNextMatches(r -> Boolean.FALSE.equals(r.getActive()))
                .verifyComplete();
    }
}
