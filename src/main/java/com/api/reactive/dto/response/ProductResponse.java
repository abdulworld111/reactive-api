package com.api.reactive.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Product details returned by the API")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "Wireless Headphones Pro")
    private String name;

    @Schema(description = "Product description")
    private String description;

    @Schema(description = "Unit price", example = "299.99")
    private BigDecimal price;

    @Schema(description = "Units in stock", example = "150")
    private Integer stock;

    @Schema(description = "Product category", example = "Electronics")
    private String category;

    @Schema(description = "SKU", example = "ELEC-WHP-001")
    private String sku;

    @Schema(description = "Active listing flag", example = "true")
    private Boolean active;

    @Schema(description = "Creation timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime createdAt;

    @Schema(description = "Last-update timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime updatedAt;
}
