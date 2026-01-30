package ru.skillbox.socialnetwork.integration.service;

//import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class StorageService {
    public void deleteUserImage(String url) {
    }

    public String saveUserImage(MultipartFile file) {
        return null;
    }

}
