## **Edge-to-Cloud AI Human Detection Alarm**<img width="100" height="100" alt="image" src="https://github.com/user-attachments/assets/56c765fd-86cf-4448-87e5-05f0f46606a0" />  <img width="100" height="100" alt="image" src="https://github.com/user-attachments/assets/b0a40ab8-e5f9-43a2-9c50-0023bbee24f8" />
🤖 ˙✧˖°📷༘ ⋆｡°
- This project is a complete, end-to-end IoT system that uses an NVIDIA Jetson Nano for real-time human detection at the edge and a decoupled, event-driven cloud backend on AWS to process events and send real-time SMS alerts.
- The backend is built with Java Spring Boot and containerized with Docker, featuring a full CI/CD pipeline for automated testing and packaging.


**User Diagram**

<img width="1280" height="720" alt="image" src="https://github.com/user-attachments/assets/5e007b4b-7e44-412c-8b14-8ae8bd8a3208" />


**System Sequence Diagram**

<img width="1280" height="720" alt="image" src="https://github.com/user-attachments/assets/ddb00f59-e00b-43c9-9351-9f825c3f7992" />

**Function Sequence Diagram**

<img width="1280" height="720" alt="image" src="https://github.com/user-attachments/assets/41e6ead9-1b4e-4eec-b818-18313bdf44c3" />


## **System Architecture**
- The application is designed with a modern, microservices-style architecture that is scalable and resilient.

- **Edge Device (NVIDIA Jetson Nano):** A Python script runs a YOLO model to detect humans. Upon detection, it captures an image and sends it to the cloud backend's REST API.

- **Spring Boot API:** Receives the image, uploads it to an S3 bucket for storage, and publishes an event message to an SQS queue.

- **Listener Service:** An independent component listens to the SQS queue. When a message arrives, it processes the detection data, saves it to a DynamoDB table, and triggers an alert.

- **Alerting:** The system uses AWS SNS to send an immediate SMS notification to a configured phone number.

## **Key Features** 
- **Real-Time Edge AI:** Performs human detection on a low-power NVIDIA Jetson Orin Nano device.

- **Event-Driven Architecture:** Uses AWS SQS to decouple the API from backend processing, ensuring no data is lost and improving system resilience.

- **Scalable Cloud Backend:** Built with Java Spring Boot and designed to handle a high throughput of detection events.

- **Automated Alerts:** Instantly notifies the user via SMS upon a new detection.

- **Secure by Design:** Implements least-privilege access using specific IAM policies and securely manages credentials using environment variables and GitHub Secrets (no hard-coded keys).

- **CI/CD Automation:** Includes a full GitHub Actions workflow to automatically test, build, and package the application into a Docker container.

- **Web Dashboard:** A simple, secure dashboard to view the 20 most recent detection events.

## **Technology Stack**

- ### **Hardware & Edge Computing:**

  - NVIDIA Jetson Orin Nano

  - Python, Optimized YOLO Model

- ### **Cloud & DevOps (AWS):**

  - **AWS Services:** S3, SQS, DynamoDB, IAM, SNS

  - **Containerization:** Docker

  - **CI/CD:** GitHub Actions

  - **Build Tool:** Maven

- ### **Backend & Databases:**

  - Frameworks: Java, Spring Boot, Spring Security

  - Architecture: REST APIs, Event-Driven Systems, Microservices

  - Databases: NoSQL (DynamoDB)

- ### **Alerting:**

AWS SNS / Twilio API

Setup and Installation
Prerequisites

Java 21

Apache Maven

Docker Desktop

An AWS account with configured credentials

A Twilio account (for the alternative SMS implementation)

1. Configure the Backend

Clone the repository:

Bash
git clone <your-repository-url>
cd jetson-dashboard
Create an application.properties file in src/main/resources/ with the following content. The application is designed to pull sensitive credentials from environment variables.

Properties
# application's name
spring.application.name=my-detection-app

# AWS Configuration
aws.region=us-east-1
aws.s3.bucket-name=<your-s3-bucket-name>
aws.dynamodb.table-name=Detections
aws.sqs.queue-name=image-processing-queue

# Twilio Configuration (if used)
twilio.destination-phone-number=<your-personal-phone-number>
2. Running with Docker (Recommended)

This is the easiest way to run the application, as the environment is completely self-contained.

Build the Docker image:

Bash
docker build -t your-username/jetson-alarm .
Run the Docker container:
Provide your credentials as environment variables.

Bash
docker run --rm -p 8080:8080 \
-e AWS_ACCESS_KEY_ID="YOUR_AWS_ACCESS_KEY" \
-e AWS_SECRET_ACCESS_KEY="YOUR_AWS_SECRET_KEY" \
-e TWILIO_ACCOUNT_SID="YOUR_TWILIO_SID" \
-e TWILIO_AUTH_TOKEN="YOUR_TWILIO_TOKEN" \
-e TWILIO_TRIAL_NUMBER="YOUR_TWILIO_NUMBER" \
your-username/jetson-alarm
3. Running from IDE (e.g., NetBeans)

Open the project in your IDE.

Configure the environment variables for your Run Configuration as described in our previous conversations (Project Properties -> Run -> Environment).

Click "Run Project".

API Usage
Image Upload

Endpoint: POST /api/upload

Request Type: multipart/form-data

Field Name: file

Description: The endpoint for the Jetson Nano (or any client) to upload a detection image.

Success Response: HTTP 202 Accepted

Future Improvements
This project serves as a strong foundation for a production-grade system. Future enhancements could include:

Comprehensive Integration Testing: Using Testcontainers to validate the full application flow in the CI/CD pipeline.

Infrastructure as Code (IaC): Defining all AWS resources (S3, SQS, etc.) in code using AWS CDK or Terraform.

Enhanced Observability: Integrating Spring Boot Actuator with Prometheus and Grafana to monitor application metrics and health.

Deployment to AWS: Deploying the container to a service like AWS App Runner or ECS for a live, scalable production environment.
