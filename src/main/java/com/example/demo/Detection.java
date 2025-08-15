/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo;
 

/**
 *
 * @author nguyenp
 */

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;


@DynamoDbBean
public class Detection {

    private String detectionId;
    private String timestamp;
    private String imageUrl;
    private String recordType; 


    @DynamoDbPartitionKey
    public String getDetectionId() { return detectionId; }
    public void setDetectionId(String detectionId) { this.detectionId = detectionId; }

    @DynamoDbSortKey
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    // This does not need a special annotation
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
     // This annotation defines the new field as a GSI partition key for an index named "detections-by-timestamp-gsi"
    @DynamoDbSecondaryPartitionKey(indexNames = "detections-by-timestamp-gsi") 
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
}
