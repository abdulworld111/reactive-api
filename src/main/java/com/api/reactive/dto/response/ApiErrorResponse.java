package com.api.reactive.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "Standard error envelope returned on API failures")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Short error token", example = "NOT_FOUND")
    private String error;

    @Schema(description = "Human-readable error message", example = "Product not found with id: 42")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/v1/products/42")
    private String path;

    @Schema(description = "Timestamp of the error")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime timestamp;

    @Schema(description = "Field-level validation errors (key = field name, value = message)")
    private Map<String, String> validationErrors;
}
