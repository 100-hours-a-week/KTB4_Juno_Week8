package com.example.demo.service;

import com.example.demo.dto.image.ImageUploadResponse;
import com.example.demo.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

import java.io.IOException;
import java.util.UUID;

@Service
public class ImageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final S3Presigner s3Presigner;

    public ImageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket}") String bucketName
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    public String createPresignedUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }

    public ImageUploadResponse uploadImage(MultipartFile image) {
        validateImage(image);

        try {
            String originalFilename = image.getOriginalFilename();
            String extension = getExtension(originalFilename);

            String objectKey = "images/" + UUID.randomUUID() + extension;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(image.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(image.getBytes())
            );

            return new ImageUploadResponse(objectKey);

        } catch (IOException error) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "이미지 업로드 중 오류가 발생했습니다."
            );
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "이미지를 첨부해주세요."
            );
        }

        String contentType = image.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "이미지 파일만 업로드할 수 있습니다."
            );
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".png";
        }

        return filename.substring(filename.lastIndexOf("."));
    }
}