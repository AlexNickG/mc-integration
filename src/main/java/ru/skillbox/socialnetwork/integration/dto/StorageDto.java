package ru.skillbox.socialnetwork.integration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Uploaded file metadata")
public class StorageDto implements Serializable {

    @Schema(
            description = "Public URL of the uploaded user image",
            example = "https://storage.yandexcloud.net/user-images-for-social-net/550e8400-e29b-41d4-a716-446655440000.jpg"
    )
    private String fileName;
}
