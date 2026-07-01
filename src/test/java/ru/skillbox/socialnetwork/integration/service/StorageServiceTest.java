package ru.skillbox.socialnetwork.integration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.socialnetwork.integration.dto.StorageDto;
import ru.skillbox.socialnetwork.integration.exception.S3UploadException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageService, "bucketName", "test-bucket");
    }

    @Test
    void saveUserImage_generatesUuidFilename_notOriginalName() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "secret-name.jpg", "image/jpeg", "data".getBytes());

        stubS3Put();
        stubS3Url("https://storage.yandexcloud.net/test-bucket/uuid.jpg");

        storageService.saveUserImage(file);

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));

        PutObjectRequest request = captor.getValue();
        assertThat(request.bucket()).isEqualTo("test-bucket");

        String key = request.key();
        assertThat(key).doesNotContain("secret-name");
        assertThat(key).endsWith(".jpg");
        assertThat(key).matches("[0-9a-f\\-]{36}\\.jpg");
    }

    @Test
    void saveUserImage_returnsUrlFromS3() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "data".getBytes());
        String expectedUrl = "https://storage.yandexcloud.net/test-bucket/some-uuid.png";

        stubS3Put();
        stubS3Url(expectedUrl);

        StorageDto result = storageService.saveUserImage(file);

        assertThat(result.getFileName()).isEqualTo(expectedUrl);
    }

    @Test
    void saveUserImage_handlesFileWithoutExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "noextension", "application/octet-stream", "data".getBytes());

        stubS3Put();
        stubS3Url("https://storage.yandexcloud.net/test-bucket/uuid");

        storageService.saveUserImage(file);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());

        String key = requestCaptor.getValue().key();
        assertThat(key).matches("[0-9a-f\\-]{36}");
        assertThat(bodyCaptor.getValue().optionalContentLength()).contains((long) file.getSize());
    }

    @Test
    void saveUserImage_uploadsFileToConfiguredBucket() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.webp", "image/webp", "image-data".getBytes());

        stubS3Put();
        stubS3Url("https://storage.yandexcloud.net/test-bucket/avatar.webp");

        storageService.saveUserImage(file);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());

        PutObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo("test-bucket");
        assertThat(request.key()).endsWith(".webp");
        assertThat(bodyCaptor.getValue().optionalContentLength()).contains((long) file.getSize());
    }

    @Test
    void saveUserImage_throwsS3UploadException_onIoException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("file.jpg");
        when(file.getInputStream()).thenThrow(new IOException("disk error"));

        assertThatThrownBy(() -> storageService.saveUserImage(file))
                .isInstanceOf(S3UploadException.class)
                .hasMessageMatching("Failed to upload file to S3: [0-9a-f\\-]{36}\\.jpg")
                .hasCauseInstanceOf(IOException.class)
                .cause().hasMessage("disk error");

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(s3Client, never()).utilities();
    }

    @Test
    void deleteUserImage_extractsFilenameFromUrl_andDeletesFromS3() {
        String url = "https://storage.yandexcloud.net/test-bucket/abc-123.jpg";
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(null);

        storageService.deleteUserImage(url);

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());

        DeleteObjectRequest request = captor.getValue();
        assertThat(request.bucket()).isEqualTo("test-bucket");
        assertThat(request.key()).isEqualTo("abc-123.jpg");
    }

    @Test
    void deleteUserImage_usesConfiguredBucketName() {
        ReflectionTestUtils.setField(storageService, "bucketName", "another-bucket");
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(null);

        storageService.deleteUserImage("https://storage.yandexcloud.net/any/file.png");

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("another-bucket");
    }

    private void stubS3Put() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());
    }

    @SuppressWarnings("unchecked")
    private void stubS3Url(String urlString) throws Exception {
        S3Utilities utilities = mock(S3Utilities.class);
        when(s3Client.utilities()).thenReturn(utilities);
        when(utilities.getUrl(any(Consumer.class))).thenReturn(URI.create(urlString).toURL());
    }
}
