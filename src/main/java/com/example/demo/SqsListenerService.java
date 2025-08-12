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

@Service
public class SqsListenerService {

    private static final Logger logger = LoggerFactory.getLogger(SqsListenerService.class);
    private final HomeController homeController; // To access the in-memory deque
    private final ObjectMapper objectMapper;

    public SqsListenerService(HomeController homeController) {
        this.homeController = homeController;
        // The object mapper needs the JavaTimeModule to understand LocalDateTime
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @SqsListener(value = "${aws.sqs.queue-name}") // Listens to the queue defined in properties
    public void processImageMessage(String message) {
        logger.info("Received new message from SQS: {}", message);
        try {
            //Parse the JSON message string back into a Map
            TypeReference<Map<String, String>> typeRef = new TypeReference<>() {};
            Map<String, String> payload = objectMapper.readValue(message, typeRef);

            String s3Url = payload.get("s3Url");
            LocalDateTime timestamp = LocalDateTime.parse(payload.get("timestamp"));

            // 
            // update the in-memory list in the HomeController.
            // FIX ME - AI PROCESSING
            DetectionEvent event = new DetectionEvent(s3Url, timestamp);
            homeController.latestDetections.addFirst(event);

            // Keep only the last 20 detections
            while (homeController.latestDetections.size() > 20) {
                homeController.latestDetections.removeLast();
            }
            logger.info("Successfully processed event for image: {}", s3Url);

        } catch (Exception e) {
            logger.error("Error processing message from SQS", e);
            
            // By throwing an exception, if a DLQ is configured, Spring Cloud AWS
            // will eventually move the message to the DLQ after enough failed attempts.
            throw new RuntimeException("Failed to process message", e);
        }
    }
}
