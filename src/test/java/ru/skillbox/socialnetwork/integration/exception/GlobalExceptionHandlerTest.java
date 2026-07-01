package ru.skillbox.socialnetwork.integration.exception;

import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import ru.skillbox.socialnetwork.integration.dto.ErrorResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleInvalidFile_returnsBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidFile(new InvalidFileException("bad file"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("bad file");
    }

    @Test
    void handleNotFound_returnsNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new ResourceNotFoundException("not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("not found");
    }

    @Test
    void handleS3Error_returnsBadGateway_forUploadException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleS3Error(new S3UploadException("avatar.png", new RuntimeException("disk full")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Failed to upload file to S3: avatar.png");
    }

    @Test
    void handleS3Error_returnsBadGateway_forDeleteException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleS3Error(new S3DeleteException("avatar.png", new RuntimeException("not found")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Failed to delete file from S3: avatar.png");
    }

    @Test
    void handleExternalServiceException_returnsBadGateway() {
        ResponseEntity<ErrorResponse> response =
                handler.handleExternalServiceException(new ExternalServiceException("hh.ru"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("External service error: hh.ru");
    }

    @Test
    void handleResponseStatusException_returnsBadRequest() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.CONFLICT, "conflict");

        ResponseEntity<ErrorResponse> response = handler.handleResponseStatusException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorMessage()).isEqualTo(exception.getMessage());
    }

    @Test
    void handleExternalServiceException_returnsBadGateway_forMethodArgumentNotValid() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getMessage()).thenReturn("validation failed");

        ResponseEntity<ErrorResponse> response = handler.handleExternalServiceException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("validation failed");
    }

    @Test
    void handleExternalServiceException_returnsBadGateway_forConstraintViolation() {
        ConstraintViolationException exception =
                new ConstraintViolationException("constraint violated", Collections.emptySet());

        ResponseEntity<ErrorResponse> response = handler.handleExternalServiceException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("constraint violated");
    }

    @Test
    void handleExternalServiceException_returnsBadGateway_forMissingServletRequestParameter() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("linkToDelete", "String");

        ResponseEntity<ErrorResponse> response = handler.handleExternalServiceException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getErrorMessage()).isEqualTo(exception.getMessage());
    }

    @Test
    void handleExternalServiceException_returnsBadGateway_forFeignException() {
        FeignException exception = mock(FeignException.class);
        when(exception.getMessage()).thenReturn("feign failure");

        ResponseEntity<ErrorResponse> response = handler.handleExternalServiceException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("feign failure");
    }

    @Test
    void handleExternalServiceException_returnsBadGateway_forAwsServiceException() {
        AwsServiceException exception = mock(AwsServiceException.class);
        when(exception.getMessage()).thenReturn("aws failure");

        ResponseEntity<ErrorResponse> response = handler.handleExternalServiceException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("aws failure");
    }

    @Test
    void handleMaxUploadSizeExceeded_returnsBadRequestWithFixedMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleMaxUploadSizeExceeded(new MaxUploadSizeExceededException(5_242_880L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("File size exceeds maximum allowed size: 5 MB");
    }
}
