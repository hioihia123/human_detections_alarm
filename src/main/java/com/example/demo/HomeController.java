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
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;




@Controller
public class HomeController {
    @Value("${aws.sqs.queue-name}")
    private String sqsQueueName;

    private final AtomicInteger latestPersonCount = new AtomicInteger(0);
    // This deque will now be populated by the SQS listener, not the controller.
    final ConcurrentLinkedDeque<DetectionEvent> latestDetections = new ConcurrentLinkedDeque<>();
    private final S3Service s3Service;
    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper(); // For converting map to JSON string
    private final AtomicReference<String> latestImageName = new AtomicReference<>();
    private final DetectionRepo detectionRepo; // Inject the repository


    // Update the constructor
    public HomeController(S3Service s3Service, SqsTemplate sqsTemplate, DetectionRepo detectionRepo) {
        this.s3Service = s3Service;
        this.sqsTemplate = sqsTemplate;
        this.detectionRepo = detectionRepo; // Assign it
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
            sqsTemplate.send(sqsQueueName,payload);

            
            // return '202 Accepted' to the client.
            // This tells the client "I've received your request and will process it."
            return ResponseEntity.accepted().body("Request accepted and is being processed.");

        } catch (IOException e) {
            e.printStackTrace();
            // In case of an error during S3 upload or sending to SQS
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Fetch the 20 most recent detections from DynamoDB
        var detectionsFromDb = detectionRepo.findTop20ByOrderByTimestampDesc();

        // Convert the Detection entities into DetectionEvent records for the view
        var detectionEvents = detectionsFromDb.stream()
            .map(d -> new DetectionEvent(d.getImageUrl(), LocalDateTime.parse(d.getTimestamp())))
            .collect(Collectors.toList());

        model.addAttribute("personCount", latestPersonCount.get());
        model.addAttribute("detectionList", detectionEvents); // Use the list from the DB
        return "dashboard";
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
        detectionRepo.deleteAll();
        System.out.println("✅ Data & Image reset");
        return "Count and Image has been reset.";
    }
}
record PersonData(int count){}