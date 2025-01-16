package com.synhrgy.recruitement;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;

import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AcceptCandidateDelegate implements JavaDelegate {
    private static final String RECRUITMENT_SERVICE_URL = "http://localhost:4003";
    private static final String POSTMARK_API_KEY = "15654fd9-38c7-4eee-bbd2-61e3bd74b6fa";// System.getenv("POSTMARK_API_KEY");
    private static final String POSTMARK_ENDPOINT = "https://api.postmarkapp.com/email";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Fetch process variables
        String applicationId = (String) execution.getVariable("applicationId");
        String email = (String) execution.getVariable("email");

        if (applicationId == null || email == null) {
            throw new IllegalArgumentException("Missing required process variables: applicationId or email.");
        }

        // Update application status to "Accepted"
        updateApplicationStatus(applicationId, "Accepted");

        // Send acceptance email
        String subject = "Application Accepted";
        String body = "Dear Applicant,\n\nWe are pleased to inform you that your application has been accepted.\n\n" +
                      "Welcome aboard!\n\nBest regards,\nRecruitment Team";
        sendEmail(email, subject, body);
    }

    private void updateApplicationStatus(String applicationId, String status) throws Exception {
        String apiUrl = RECRUITMENT_SERVICE_URL + "/application/" + applicationId;
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Create request payload
        JSONObject requestBody = new JSONObject();
        requestBody.put("status", status);

        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.toString().getBytes());
            os.flush();
        }

        // Handle response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
            System.out.println("API Response: " + response);
        } else {
            throw new RuntimeException("Failed to update application status: HTTP code " + responseCode);
        }
    }

    private void sendEmail(String recipientEmail, String subject, String body) throws Exception {
        if (POSTMARK_API_KEY == null) {
            throw new IllegalStateException("Missing POSTMARK_API_KEY environment variable.");
        }

        // Create email payload
        JSONObject emailPayload = new JSONObject();
        emailPayload.put("From", "recruitment@company.com");
        emailPayload.put("To", recipientEmail);
        emailPayload.put("Subject", subject);
        emailPayload.put("TextBody", body);

        // Send email
        URL url = new URL(POSTMARK_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("X-Postmark-Server-Token", POSTMARK_API_KEY);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(emailPayload.toString().getBytes());
            os.flush();
        }

        // Handle email response
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            System.out.println("Acceptance email sent successfully to " + recipientEmail);
        } else {
            System.err.println("Failed to send acceptance email. HTTP Response Code: " + responseCode);
        }
    }
}
