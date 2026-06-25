package com.api.reactive.service;

import com.api.reactive.dto.request.ProductFilterRequest;
import com.api.reactive.dto.request.ProductRequest;
import com.api.reactive.dto.response.PagedResponse;
import com.api.reactive.dto.response.ProductResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Mono<ProductResponse>       createProduct(ProductRequest request);
    Mono<ProductResponse>       getProductById(Long id);
    Mono<ProductResponse>       getProductBySku(String sku);
    Mono<PagedResponse<ProductResponse>> getAllProducts(ProductFilterRequest filter);
    Flux<ProductResponse>       getProductsByCategory(String category);
    Mono<ProductResponse>       updateProduct(Long id, ProductRequest request);
    Mono<ProductResponse>       patchProductStock(Long id, int quantity);
    Mono<ProductResponse>       toggleProductStatus(Long id);
    Mono<Void>                  deleteProduct(Long id);
}
