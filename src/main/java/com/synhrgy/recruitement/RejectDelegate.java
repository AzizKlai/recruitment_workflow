package com.synhrgy.recruitement;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;

import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//import io.github.cdimascio.dotenv.Dotenv;

public class RejectDelegate implements JavaDelegate {
    private static final String RECRUITMENT_SERVICE_URL = "http://localhost:4003";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String applicationId = (String) execution.getVariable("applicationId");
        String email = (String) execution.getVariable("email");
        Boolean meetsQualifications = (Boolean) execution.getVariable("meetsQualifications");

        // Update qualifications status via API
        String apiUrl = RECRUITMENT_SERVICE_URL + "/updateQualifications/" + applicationId;
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JSONObject requestBody = new JSONObject();
        if (meetsQualifications) {
            requestBody.put("meetsQualifications", "Qualified");
        } else
        requestBody.put("meetsQualifications", "NotQualified");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.toString().getBytes());
            os.flush();
        }

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
            throw new RuntimeException("Failed to update qualifications: HTTP code " + responseCode);
        }

        // Send rejection email
        String subject = "Application Status Update";
        String body = "Dear Applicant,\n\nWe regret to inform you that your application has not met the required qualifications.\n\nThank you for your interest.\n\nBest regards,\nRecruitment Team";

        sendEmail("mohamedaziz.klai@insat.ucar.tn", subject, body);
    }

    private static void sendEmail(String recipientEmail, String subject, String body) throws Exception {
    
            // Load environment variables from .env file
            //Dotenv dotenv = Dotenv.load();
            //String postmarkApiKey = dotenv.get("POSTMARK_API_KEY");
            String postmarkApiKey = "a4c65876-716d-4202-864c-0df9d87357d5";
            // API Endpoint
            String postmarkEndpoint = "https://api.postmarkapp.com/email";
    
            // Create the email payload
            JSONObject emailPayload = new JSONObject();
            emailPayload.put("From", "mohamedaziz.klai@insat.ucar.tn");
            emailPayload.put("To", recipientEmail);
            emailPayload.put("Subject", subject);
            emailPayload.put("TextBody", body);
    
            try {
                // Open HTTP connection
                URL url = new URL(postmarkEndpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("X-Postmark-Server-Token", postmarkApiKey);
                connection.setDoOutput(true);
    
                // Send the payload
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(emailPayload.toString().getBytes());
                    os.flush();
                }
    
                // Handle the response
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    System.out.println("Email sent successfully!");
                } else {
                    System.err.println("Failed to send email. HTTP Response Code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error sending email: " + e.getMessage());
            }
        }
        public static void main(String[] args) throws Exception {
        String applicationId = "3";
        Boolean meetsQualifications = false;

        // Update qualifications status via API
        String apiUrl = RECRUITMENT_SERVICE_URL + "/updateQualifications/" + applicationId;
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JSONObject requestBody = new JSONObject();
        requestBody.put("meetsQualifications", meetsQualifications);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.toString().getBytes());
            os.flush();
        }

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
            throw new RuntimeException("Failed to update qualifications: HTTP code " + responseCode);
        }

        // Send rejection email
        String subject = "Application Status Update";
        String body = "Dear Applicant,\n\nWe regret to inform you that your application has not met the required qualifications.\n\nThank you for your interest.\n\nBest regards,\nRecruitment Team";

        sendEmail("mohamedaziz.klai@insat.ucar.tn", subject, body);
    }

    
        
}
