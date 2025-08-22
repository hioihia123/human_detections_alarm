/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo;

/**
 * ewq
 * @author nguyenp
 */


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;

import java.io.IOException;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

public String uploadFile(String fileName, MultipartFile file) throws IOException {
    // Build the PutObjectRequest
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .contentType(file.getContentType()) // optional but useful
        .build();

    // Upload file (using fromBytes)
    s3Client.putObject(
        putObjectRequest,
        RequestBody.fromBytes(file.getBytes())
    );

    // Get bucket location
    GetBucketLocationResponse locationResponse = s3Client.getBucketLocation(
        GetBucketLocationRequest.builder()
            .bucket(bucketName)
            .build()
    );

    String bucketRegion = locationResponse.locationConstraintAsString();

    // AWS quirk: us-east-1 is sometimes null or "US"
    if (bucketRegion == null || bucketRegion.equals("US") || bucketRegion.isEmpty()) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
    } else {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, bucketRegion, fileName);
    }
}


}
