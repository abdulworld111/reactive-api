package com.api.reactive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Schema(description = "Filter and pagination parameters for product listing")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {

    @Schema(description = "Filter by name (partial, case-insensitive)", example = "headphone")
    private String name;

    @Schema(description = "Filter by category (exact, case-insensitive)", example = "Electronics")
    private String category;

    @Schema(description = "Filter by active status", example = "true")
    private Boolean active;

    @Schema(description = "Minimum price filter", example = "10.00")
    @DecimalMin(value = "0.0", message = "Min price cannot be negative")
    private BigDecimal minPrice;

    @Schema(description = "Maximum price filter", example = "500.00")
    @DecimalMin(value = "0.0", message = "Max price cannot be negative")
    private BigDecimal maxPrice;

    @Schema(description = "Zero-based page number", example = "0", defaultValue = "0")
    @PositiveOrZero(message = "Page must be >= 0")
    @Builder.Default
    private int page = 0;

    @Schema(description = "Number of records per page (max 100)", example = "20", defaultValue = "20")
    @Min(value = 1, message = "Size must be >= 1")
    @Builder.Default
    private int size = 20;

    @Schema(description = "Sort field", example = "name", allowableValues = {"id","name","price","stock","category","createdAt"})
    @Builder.Default
    private String sortBy = "id";

    @Schema(description = "Sort direction", example = "asc", allowableValues = {"asc","desc"})
    @Builder.Default
    private String sortDir = "asc";
}
