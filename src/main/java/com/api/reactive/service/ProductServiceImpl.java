package com.api.reactive.service;

import com.api.reactive.dto.request.ProductFilterRequest;
import com.api.reactive.dto.request.ProductRequest;
import com.api.reactive.dto.response.PagedResponse;
import com.api.reactive.dto.response.ProductResponse;
import com.api.reactive.entity.Product;
import com.api.reactive.exception.BusinessException;
import com.api.reactive.exception.DuplicateResourceException;
import com.api.reactive.exception.ResourceNotFoundException;
import com.api.reactive.mapper.ProductMapper;
import com.api.reactive.repository.ProductRepository;
import com.api.reactive.repository.ProductRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository        productRepository;
    private final ProductRepositoryCustom  productRepositoryCustom;
    private final ProductMapper            productMapper;

    // ── Create ───────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Mono<ProductResponse> createProduct(ProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSku());

        return productRepository.existsBySku(request.getSku())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new DuplicateResourceException("Product", "SKU", request.getSku()));
                    }
                    Product product = productMapper.toEntity(request);
                    product.setCreatedAt(OffsetDateTime.now());
                    product.setUpdatedAt(OffsetDateTime.now());
                    return productRepository.save(product);
                })
                .map(productMapper::toResponse)
                .doOnSuccess(p -> log.info("Product created with id: {}", p.getId()));
    }

    // ── Read ─────────────────────────────────────────────────────────────────────
    @Override
    public Mono<ProductResponse> getProductById(Long id) {
        log.debug("Fetching product by id: {}", id);
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product", "id", id)))
                .map(productMapper::toResponse);
    }

    @Override
    public Mono<ProductResponse> getProductBySku(String sku) {
        log.debug("Fetching product by SKU: {}", sku);
        return productRepository.findBySku(sku)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product", "SKU", sku)))
                .map(productMapper::toResponse);
    }

    @Override
    public Mono<PagedResponse<ProductResponse>> getAllProducts(ProductFilterRequest filter) {
        log.debug("Fetching products with filter: page={}, size={}", filter.getPage(), filter.getSize());

        // Clamp page size
        int size = Math.min(filter.getSize(), 100);
        filter.setSize(size);

        Mono<Long>          totalMono   = productRepositoryCustom.countByFilter(filter);
        Flux<ProductResponse> contentFlux = productRepositoryCustom
                .findByFilter(filter)
                .map(productMapper::toResponse);

        return Mono.zip(
                contentFlux.collectList(),
                totalMono
        ).map(tuple -> {
            List<ProductResponse> content = tuple.getT1();
            long total = tuple.getT2();
            return PagedResponse.of(content, filter.getPage(), filter.getSize(), total);
        });
    }

    @Override
    public Flux<ProductResponse> getProductsByCategory(String category) {
        log.debug("Fetching products by category: {}", category);
        return productRepository.findByCategory(category)
                .map(productMapper::toResponse);
    }

    // ── Update ────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Mono<ProductResponse> updateProduct(Long id, ProductRequest request) {
        log.info("Updating product id: {}", id);

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product", "id", id)))
                .flatMap(existing -> productRepository.existsBySkuAndIdNot(request.getSku(), id)
                        .flatMap(skuTaken -> {
                            if (Boolean.TRUE.equals(skuTaken)) {
                                return Mono.error(new DuplicateResourceException("Product", "SKU", request.getSku()));
                            }
                            productMapper.updateEntity(request, existing);
                            existing.setUpdatedAt(OffsetDateTime.now());
                            return productRepository.save(existing);
                        })
                )
                .map(productMapper::toResponse)
                .doOnSuccess(p -> log.info("Product updated: {}", p.getId()));
    }

    @Override
    @Transactional
    public Mono<ProductResponse> patchProductStock(Long id, int quantity) {
        log.info("Patching stock for product id: {} by {}", id, quantity);

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product", "id", id)))
                .flatMap(product -> {
                    int newStock = product.getStock() + quantity;
                    if (newStock < 0) {
                        return Mono.error(new BusinessException(
                                "Insufficient stock. Available: " + product.getStock() + ", requested: " + Math.abs(quantity)));
                    }
                    product.setStock(newStock);
                    product.setUpdatedAt(OffsetDateTime.now());
                    return productRepository.save(product);
                })
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<ProductResponse> toggleProductStatus(Long id) {
        log.info("Toggling status for product id: {}", id);

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product", "id", id)))
                .flatMap(product -> {
                    product.setActive(!Boolean.TRUE.equals(product.getActive()));
                    product.setUpdatedAt(OffsetDateTime.now());
                    return productRepository.save(product);
                })
                .map(productMapper::toResponse)
                .doOnSuccess(p -> log.info("Product {} status toggled to: {}", p.getId(), p.getActive()));
    }

    // ── Delete ────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Mono<Void> deleteProduct(Long id) {
        log.info("Deleting product id: {}", id);

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product", "id", id)))
                .flatMap(productRepository::delete)
                .doOnSuccess(v -> log.info("Product deleted: {}", id));
    }
}
