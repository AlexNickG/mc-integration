package ru.skillbox.socialnetwork.integration.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.socialnetwork.integration.dto.StorageDto;
import ru.skillbox.socialnetwork.integration.service.StorageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storage")
@Validated
@Tag(name = "Storage API v.1", description = "Client API version v.1")
public class StorageController {

    private final StorageService storageService;

    @Operation(
            summary = "Upload user image",
            description = "Uploads user image to S3 storage and returns the public file URL. Maximum file size: 5Mb"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "user image has been saved to S3 storage",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StorageDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "S3 storage is unavailable"
            )
    })
    @PostMapping(value = "/uploadUserImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StorageDto> uploadUserImage(
            @Parameter(
                    description = "User image file. Maximum size: 5 Mb",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(storageService.saveUserImage(file));
    }

    @Operation(
            summary = "Delete user image",
            description = "Deletes user image from S3 storage by public URL"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Image deleted"),
            @ApiResponse(responseCode = "400", description = "Missing or blank image URL"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "502", description = "S3 storage is unavailable")
    })
    @DeleteMapping("/deleteByLink")
    public ResponseEntity<Void> deleteByLink(
            @Parameter(
                    description = "Public URL of the image to delete",
                    example = "https://storage.yandexcloud.net/user-images-for-social-net/550e8400-e29b-41d4-a716-446655440000.jpg",
                    required = true
            )
            @RequestParam("linkToDelete")
            @NotBlank(message = "linkToDelete is required")
            String linkToDelete
    ) {
        storageService.deleteUserImage(linkToDelete);
        return ResponseEntity.noContent().build();
    }
}
