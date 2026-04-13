package ru.skillbox.socialnetwork.integration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.socialnetwork.integration.dto.StorageDto;
import ru.skillbox.socialnetwork.integration.exception.S3UploadException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public void deleteUserImage(String url) {
        String fileName = StringUtils.getFilename(url);
        log.info("Deleting file from S3: {}", fileName);
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    public StorageDto saveUserImage(MultipartFile file) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + (extension != null ? "." + extension : "");

        log.info("Uploading file to S3: {}", fileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        try {
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            log.error("Failed to upload file to S3: {}", fileName, e);
            throw new S3UploadException(fileName, e);
        }

        String fileUrl = s3Client.utilities().getUrl(b -> b.bucket(bucketName).key(fileName)).toString();
        StorageDto storageDto = new StorageDto();
        storageDto.setFileName(fileUrl);
        return storageDto;
    }
}
