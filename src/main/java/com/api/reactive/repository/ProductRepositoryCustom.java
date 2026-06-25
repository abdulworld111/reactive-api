package com.api.reactive.repository;

import com.api.reactive.dto.request.ProductFilterRequest;
import com.api.reactive.entity.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepositoryCustom {
    Flux<Product> findByFilter(ProductFilterRequest filter);
    Mono<Long> countByFilter(ProductFilterRequest filter);
}
