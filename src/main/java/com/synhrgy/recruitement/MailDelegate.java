package com.synhrgy.recruitement;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//import io.github.cdimascio.dotenv.Dotenv;

public class MailDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String email = (String) execution.getVariable("email");
        String subject = (String) execution.getVariable("mailSubject");
        String mailBody = (String) execution.getVariable("mailBody");
        sendEmail(email, subject, mailBody);
    }

    private static void sendEmail(String recipientEmail, String subject, String body) throws Exception {
    
            // Load environment variables from .env file
            //Dotenv dotenv = Dotenv.load();
            //String postmarkApiKey = dotenv.get("POSTMARK_API_KEY");
            String postmarkApiKey = "15654fd9-38c7-4eee-bbd2-61e3bd74b6fa";
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
        
}
