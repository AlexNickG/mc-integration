package ru.skillbox.socialnetwork.integration.exception;

public class S3DeleteException extends RuntimeException {

    public S3DeleteException(String fileName, Throwable cause) {
        super("Failed to delete file from S3: " + fileName, cause);
    }
}
