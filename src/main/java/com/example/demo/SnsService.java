/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
/**
 *
 * @author nguyenp
 */
@Service //Tell Spring to create a bean of this class
public class SnsService {
    
    private static final Logger logger = LoggerFactory.getLogger(SnsService.class);
    private final SnsClient snsClient; 
    
    @Value("${aws.sns.phone-number}")
    private String phoneNumber;
    
    public SnsService(SnsClient snsClient){
        this.snsClient = snsClient;
    }
    
    public void sendSms(String message){
        try{
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(phoneNumber)
                    .build();
            
            PublishResponse result = snsClient.publish(request);
            logger.info("SMS sent successfully! Message ID: {}", result.messageId());
        } catch (SnsException e){
            logger.error("Error sending SMS: {}", e.awsErrorDetails().errorMessage());
            System.err.println(e.awsErrorDetails().errorMessage());
        }   
    }
    
}
