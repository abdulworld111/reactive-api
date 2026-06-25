package com.api.reactive.controller;

import com.api.reactive.dto.request.ProductFilterRequest;
import com.api.reactive.dto.request.ProductRequest;
import com.api.reactive.dto.response.ApiErrorResponse;
import com.api.reactive.dto.response.PagedResponse;
import com.api.reactive.dto.response.ProductResponse;
import com.api.reactive.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Products", description = "CRUD operations for the product catalog")
public class ProductController {

    private final ProductService productService;

    // ── POST /api/v1/products ─────────────────────────────────────────────────────
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product", description = "Creates a product and returns the persisted resource.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "SKU already exists",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public Mono<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("POST /api/v1/products — SKU: {}", request.getSku());
        return productService.createProduct(request);
    }

    // ── GET /api/v1/products ──────────────────────────────────────────────────────
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all products", description = "Returns a paginated, filterable list of products.")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    public Mono<PagedResponse<ProductResponse>> getAllProducts(
            @Parameter(description = "Filter by name (partial match)")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by category")
            @RequestParam(required = false) String category,

            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean active,

            @Parameter(description = "Minimum price")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Maximum price")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (max 100)")
            @RequestParam(defaultValue = "20") @Min(1) int size,

            @Parameter(description = "Sort field: id, name, price, stock, category, createdAt")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        ProductFilterRequest filter = ProductFilterRequest.builder()
                .name(name).category(category).active(active)
                .minPrice(minPrice).maxPrice(maxPrice)
                .page(page).size(size).sortBy(sortBy).sortDir(sortDir)
                .build();

        log.debug("GET /api/v1/products — filter: {}", filter);
        return productService.getAllProducts(filter);
    }

    // ── GET /api/v1/products/{id} ─────────────────────────────────────────────────
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get product by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public Mono<ProductResponse> getProductById(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id) {
        return productService.getProductById(id);
    }

    // ── GET /api/v1/products/sku/{sku} ────────────────────────────────────────────
    @GetMapping(value = "/sku/{sku}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get product by SKU")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public Mono<ProductResponse> getProductBySku(
            @Parameter(description = "Stock Keeping Unit", required = true) @PathVariable String sku) {
        return productService.getProductBySku(sku);
    }

    // ── GET /api/v1/products/category/{category} ──────────────────────────────────
    @GetMapping(value = "/category/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all products in a category", description = "Returns a Flux stream of products.")
    @ApiResponse(responseCode = "200", description = "Products stream returned")
    public Flux<ProductResponse> getProductsByCategory(
            @Parameter(description = "Category name", required = true) @PathVariable String category) {
        return productService.getProductsByCategory(category);
    }

    // ── PUT /api/v1/products/{id} ─────────────────────────────────────────────────
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Full update of a product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "SKU already taken",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public Mono<ProductResponse> updateProduct(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        log.info("PUT /api/v1/products/{} — SKU: {}", id, request.getSku());
        return productService.updateProduct(id, request);
    }

    // ── PATCH /api/v1/products/{id}/stock ────────────────────────────────────────
    @PatchMapping(value = "/{id}/stock", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Adjust stock quantity",
               description = "Pass a positive value to add stock, negative to remove it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock adjusted"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public Mono<ProductResponse> patchStock(
            @PathVariable Long id,
            @Parameter(description = "Quantity delta (positive = add, negative = remove)", required = true)
            @RequestParam int quantity) {
        return productService.patchProductStock(id, quantity);
    }

    // ── PATCH /api/v1/products/{id}/toggle-status ─────────────────────────────────
    @PatchMapping(value = "/{id}/toggle-status", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Toggle product active/inactive status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status toggled"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public Mono<ProductResponse> toggleStatus(@PathVariable Long id) {
        return productService.toggleProductStatus(id);
    }

    // ── DELETE /api/v1/products/{id} ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a product by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public Mono<Void> deleteProduct(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id) {
        log.info("DELETE /api/v1/products/{}", id);
        return productService.deleteProduct(id);
    }
}
