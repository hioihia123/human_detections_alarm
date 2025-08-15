package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DetectionRepo {

    private final DynamoDbTable<Detection> detectionTable;

    /**
     * Constructor that sets up the repository.
     * @param enhancedClient The DynamoDB Enhanced Client bean provided by Spring Cloud AWS.
     * @param tableName The name of the DynamoDB table from application.properties.
     */
    public DetectionRepo(DynamoDbEnhancedClient enhancedClient,
                               @Value("${aws.dynamodb.table-name}") String tableName) {
        
        // The TableSchema.fromBean(Detection.class) automatically maps the Detection class to the table structure.
        this.detectionTable = enhancedClient.table(tableName, TableSchema.fromBean(Detection.class));
    }

    /**
     * Saves a new detection event to the DynamoDB table.
     * @param detection The Detection object to save.
     */
    public void save(Detection detection) {
        detectionTable.putItem(detection);
    }

    /**
     * Fetches all detections, sorts them by timestamp in memory, and returns the 20 most recent.
     * performs a DynamoDB Scan operation
     * @return A list of the 20 most recent Detection objects.
     */
    public List<Detection> findTop20ByOrderByTimestampDesc() {
        // Scan the entire table to get all items.
        Iterable<Detection> allDetectionsIterable = detectionTable.scan(ScanEnhancedRequest.builder().build()).items();

        // Convert the results to a List.
        List<Detection> allDetections = new ArrayList<>();
        allDetectionsIterable.forEach(allDetections::add);

        // Sort the list by timestamp in descending order (newest first).
        allDetections.sort(Comparator.comparing(Detection::getTimestamp).reversed());

        // Return the first 20 items from the sorted list.
        return allDetections.stream()
                .limit(20)
                .collect(Collectors.toList());    

//         //Build the query
//         QueryEnhancedRequest request = QueryEnhancedRequest.builder()
//                 .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue("detectionId")))
//                 .scanIndexForward(false)
//                 .limit(25)
//                 .build();
//         
//         //Run the query & collect items
//         return detectionTable.query(request)
//                 .items()
//                 .stream()
//                 .collect(Collectors.toList());
            
    }
}
