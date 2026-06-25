package com.api.reactive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Schema(description = "Payload for creating or updating a product")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @Schema(description = "Product display name", example = "Wireless Headphones Pro")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @Schema(description = "Detailed description of the product", example = "Premium noise-cancelling headphones")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Schema(description = "Unit price (≥ 0)", example = "299.99")
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Price must have at most 17 integer and 2 fraction digits")
    private BigDecimal price;

    @Schema(description = "Units in stock (≥ 0)", example = "150")
    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @Schema(description = "Product category", example = "Electronics")
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Schema(description = "Unique Stock Keeping Unit identifier", example = "ELEC-WHP-001")
    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "SKU must contain only uppercase letters, digits, and hyphens")
    private String sku;

    @Schema(description = "Whether the product is listed as active", example = "true")
    private Boolean active = Boolean.TRUE;
}
