package com.example.demo;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author nguyenp
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID; // Import UUID

@Service
public class SqsListenerService {

    private static final Logger logger = LoggerFactory.getLogger(SqsListenerService.class);
    private final HomeController homeController; // To access the in-memory deque
    private final ObjectMapper objectMapper;
    private final DetectionRepo detectionRepo; // Inject the repository
    
    public SqsListenerService(HomeController homeController, DetectionRepo detectionRepo) {
        this.homeController = homeController;
        this.detectionRepo = detectionRepo;
        // The object mapper needs the JavaTimeModule to understand LocalDateTime
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @SqsListener(value = "${aws.sqs.queue-name}")
    public void processImageMessage(String message) {
        logger.info("Received new message from SQS: {}", message);
        try {
            TypeReference<Map<String, String>> typeRef = new TypeReference<>() {};
            Map<String, String> payload = objectMapper.readValue(message, typeRef);

            String s3Url = payload.get("s3Url");
            String timestamp = payload.get("timestamp");

            // --- Database Interaction ---
            // 1. Create a new Detection object
            Detection detection = new Detection();
            detection.setDetectionId(UUID.randomUUID().toString()); // Generate a unique ID
            detection.setTimestamp(timestamp);
            detection.setImageUrl(s3Url);

            // 2. Save the object to DynamoDB
            detectionRepo.save(detection);
            logger.info("Successfully saved detection {} to DynamoDB.", detection.getDetectionId());
            // --- End of Database Interaction ---


//            // the dashboard will fetch from the DB.
//            // For now, we can leave it so the dashboard has immediate updates.
//            DetectionEvent event = new DetectionEvent(s3Url, LocalDateTime.parse(timestamp));
//            homeController.latestDetections.addFirst(event);
//
//            while (homeController.latestDetections.size() > 20) {
//                homeController.latestDetections.removeLast();
//            }
//            logger.info("Successfully processed event for image: {}", s3Url);

        } catch (Exception e) {
            logger.error("Error processing message from SQS", e);
            throw new RuntimeException("Failed to process message", e);
        }
    }
}
