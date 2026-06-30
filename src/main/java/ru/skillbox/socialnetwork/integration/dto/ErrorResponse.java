package ru.skillbox.socialnetwork.integration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API error response")
public class ErrorResponse {

    @Schema(
            description = "Human-readable error message",
            example = "linkToDelete is required"
    )
    private String errorMessage;
}
