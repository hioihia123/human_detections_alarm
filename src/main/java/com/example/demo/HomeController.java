package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.atomic.AtomicReference;



@Controller
public class HomeController {

    private final AtomicInteger latestPersonCount = new AtomicInteger(0);
    // This deque will now be populated by the SQS listener, not the controller.
    final ConcurrentLinkedDeque<DetectionEvent> latestDetections = new ConcurrentLinkedDeque<>();
    private final S3Service s3Service;
    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper(); // For converting map to JSON string
    private final AtomicReference<String> latestImageName = new AtomicReference<>();


    // Inject SqsTemplate
    public HomeController(S3Service s3Service, SqsTemplate sqsTemplate) {
        this.s3Service = s3Service;
        this.sqsTemplate = sqsTemplate;
    }

    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<String> receiveImage(@RequestParam("file") MultipartFile file) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String fileName = timestamp + "_" + file.getOriginalFilename();
        
        this.latestImageName.set(fileName);

        try {
            // Upload the file to S3 immediately
            String s3Url = s3Service.uploadFile(fileName, file);
            System.out.println("File uploaded to S3: " + s3Url);
            
            String imagePath = "/uploads/" + fileName;
            
            latestDetections.addFirst(new DetectionEvent(s3Url, LocalDateTime.now()));
            
            while(latestDetections.size() > 20){
                latestDetections.removeLast();
            }
            //Create a message payload (as a Map)
            Map<String, String> messagePayload = Map.of(
                    "s3Url", s3Url,
                    "timestamp", LocalDateTime.now().toString()
            );

            //Send the message to the SQS queue. --> asynchronous operation.
            String payload = objectMapper.writeValueAsString(messagePayload);
            sqsTemplate.send(payload);

            
            // return '202 Accepted' to the client.
            // This tells the client "I've received your request and will process it."
            return ResponseEntity.accepted().body("Request accepted and is being processed.");

        } catch (IOException e) {
            e.printStackTrace();
            // In case of an error during S3 upload or sending to SQS
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }

    // This method remains the same, but its data is now populated by the listener.
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("personCount", latestPersonCount.get());
        model.addAttribute("detectionList", latestDetections);
        return "dashboard"; //
    }

    // 
    @PostMapping("/api/data")
    @ResponseBody
    public String receiveData(@RequestBody PersonData data) {
        System.out.println("Received person count: " + data.count());
        this.latestPersonCount.set(data.count()); //
        return "Data received successfully!";
    }
    
    @GetMapping("/api/count")
    @ResponseBody
    public int getLatestCount(){
        return this.latestPersonCount.get(); //
    }
    
    @PostMapping("/api/reset")
    @ResponseBody
    public String resetCount() {
        this.latestPersonCount.set(0);
        this.latestDetections.clear(); //
        System.out.println("✅ Data & Image reset");
        return "Count and Image has been reset.";
    }
}
record PersonData(int count){}