package ru.skillbox.socialnetwork.integration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.socialnetwork.integration.dto.StorageDto;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public void deleteUserImage(String url) {
        String fileName = StringUtils.getFilename(url);

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(fileName).build();
        DeleteObjectResponse response = s3Client.deleteObject(deleteObjectRequest);
    }

    public StorageDto saveUserImage(MultipartFile file) {
        StorageDto storageDto = new StorageDto();
        String fileName = file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        try {
            PutObjectResponse response = s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            System.out.println("IOException" + e.fillInStackTrace());
            throw new RuntimeException(e);
        }
        storageDto.setFileName(s3Client.utilities().getUrl(b -> b.bucket(bucketName).key(fileName)).toString());
        //System.out.println(result);

        return storageDto;
    }
}
