package com.synhrgy.recruitement;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterApplicationDelegate implements JavaDelegate {
    private static final String RECRUITMENT_SERVICE_URL = "http://localhost:4003";
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        
        
        String firstName = (String) execution.getVariable("first_name");
        String lastName = (String) execution.getVariable("last_name");
        String email = (String) execution.getVariable("email");
        String role = (String) execution.getVariable("role");

        // payload
        JSONObject payload = new JSONObject();
        payload.put("candidateName", firstName + " " + lastName);
        payload.put("email", email);
        payload.put("role", role);
        System.out.println(payload.toString());
        // Set up the HTTP connection
        URL url = new URL(RECRUITMENT_SERVICE_URL+"/submitApplication");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Configure the connection
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Get the response code and log it
        int responseCode = connection.getResponseCode();
        if (responseCode == 200 || responseCode == 201) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Parse the response to get the application ID
                JSONObject responseJson = new JSONObject(response.toString());
                JSONObject applicationJson = responseJson.getJSONObject("application");
                String applicationId = applicationJson.getString("id");

                // Store the application ID as a process variable
                execution.setVariable("applicationId", applicationId);

                System.out.println("Application submitted successfully. Application ID: " + applicationId);
            }
        } else {
            throw new RuntimeException("Failed to submit application. HTTP response code: " + responseCode);
        }
    }
}

