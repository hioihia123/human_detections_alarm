package com.example.demo;

import java.time.LocalDateTime;

//Recrod holds the URL of the image & the time it was captured
public record DetectionEvent(String imagePath, LocalDateTime timestamp){
    
}
        
