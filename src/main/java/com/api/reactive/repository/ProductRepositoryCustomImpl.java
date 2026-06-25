package com.api.reactive.repository;

import com.api.reactive.dto.request.ProductFilterRequest;
import com.api.reactive.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private static final List<String> ALLOWED_SORT_FIELDS =
            List.of("id", "name", "price", "stock", "category", "created_at");

    private final DatabaseClient databaseClient;

    @Override
    public Flux<Product> findByFilter(ProductFilterRequest filter) {
        var params = new ArrayList<Object>();
        String where = buildWhere(filter, params);

        String sortCol  = sanitizeSortField(filter.getSortBy());
        String sortDir  = "desc".equalsIgnoreCase(filter.getSortDir()) ? "DESC" : "ASC";
        int    offset   = filter.getPage() * filter.getSize();

        String sql = "SELECT * FROM products " + where
                + " ORDER BY " + sortCol + " " + sortDir
                + " LIMIT " + filter.getSize() + " OFFSET " + offset;

        log.debug("Dynamic query: {}, params: {}", sql, params);

        var spec = databaseClient.sql(sql);
        spec = bindParams(spec, params);

        return spec.map((row, meta) -> Product.builder()
                .id(row.get("id", Long.class))
                .name(row.get("name", String.class))
                .description(row.get("description", String.class))
                .price(row.get("price", BigDecimal.class))
                .stock(row.get("stock", Integer.class))
                .category(row.get("category", String.class))
                .sku(row.get("sku", String.class))
                .active(row.get("active", Boolean.class))
                .createdAt(row.get("created_at", java.time.OffsetDateTime.class))
                .updatedAt(row.get("updated_at", java.time.OffsetDateTime.class))
                .build()
        ).all();
    }

    @Override
    public Mono<Long> countByFilter(ProductFilterRequest filter) {
        var params = new ArrayList<Object>();
        String where = buildWhere(filter, params);

        String sql = "SELECT COUNT(*) FROM products " + where;

        var spec = databaseClient.sql(sql);
        spec = bindParams(spec, params);

        return spec.map((row, meta) -> row.get(0, Long.class)).one();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private String buildWhere(ProductFilterRequest filter, List<Object> params) {
        var clauses = new ArrayList<String>();
        int i = 1;

        if (filter.getName() != null && !filter.getName().isBlank()) {
            clauses.add("LOWER(name) LIKE LOWER($" + i++ + ")");
            params.add("%" + filter.getName().trim() + "%");
        }
        if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
            clauses.add("LOWER(category) = LOWER($" + i++ + ")");
            params.add(filter.getCategory().trim());
        }
        if (filter.getActive() != null) {
            clauses.add("active = $" + i++ );
            params.add(filter.getActive());
        }
        if (filter.getMinPrice() != null) {
            clauses.add("price >= $" + i++);
            params.add(filter.getMinPrice());
        }
        if (filter.getMaxPrice() != null) {
            clauses.add("price <= $" + i++);
            params.add(filter.getMaxPrice());
        }

        return clauses.isEmpty() ? "" : "WHERE " + String.join(" AND ", clauses);
    }

    private DatabaseClient.GenericExecuteSpec bindParams(
            DatabaseClient.GenericExecuteSpec spec, List<Object> params) {

        for (int i = 0; i < params.size(); i++) {
            spec = spec.bind(i, params.get(i));
        }
        return spec;
    }

    private String sanitizeSortField(String sortBy) {
        if (sortBy == null) return "id";
        String mapped = switch (sortBy.toLowerCase()) {
            case "createdat", "created_at" -> "created_at";
            case "updatedat", "updated_at" -> "updated_at";
            default -> sortBy.toLowerCase();
        };
        return ALLOWED_SORT_FIELDS.contains(mapped) ? mapped : "id";
    }
}
