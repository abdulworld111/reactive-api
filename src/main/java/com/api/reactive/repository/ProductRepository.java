package com.api.reactive.repository;

import com.api.reactive.entity.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

    Mono<Boolean> existsBySku(String sku);

    Mono<Boolean> existsBySkuAndIdNot(String sku, Long id);

    Mono<Product> findBySku(String sku);

    Flux<Product> findByCategory(String category);

    Flux<Product> findByActive(Boolean active);

    @Query("SELECT COUNT(*) FROM products WHERE (:name IS NULL OR LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:category IS NULL OR LOWER(category) = LOWER(:category)) " +
           "AND (:active IS NULL OR active = :active) " +
           "AND (:minPrice IS NULL OR price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR price <= :maxPrice)")
    Mono<Long> countByFilter(String name, String category, Boolean active,
                              java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);
}
