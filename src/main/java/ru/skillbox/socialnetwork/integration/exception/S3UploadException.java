package ru.skillbox.socialnetwork.integration.exception;

public class S3UploadException extends RuntimeException {

    public S3UploadException(String fileName, Throwable cause) {
        super("Failed to upload file to S3: " + fileName, cause);
    }
}