package com.synhrgy.recruitement;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendCandidateDocsDelegate implements JavaDelegate {
    private static final String SERVICE_URL = System.getenv("CANDIDATE_SERVICE_URL") != null
            ? System.getenv("CANDIDATE_SERVICE_URL")
            : "http://localhost:8080";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Fetch the candidate ID from process variables
        String candidateId = (String) execution.getVariable("candidateId");

        if (candidateId == null) {
            throw new IllegalArgumentException("Candidate ID is missing from process variables.");
        }

        // Construct the API endpoint URL
        String apiUrl = SERVICE_URL + "/generate_and_send_candidate_docs/" + candidateId;

        // Make an HTTP POST request to the API
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000); // 5 seconds timeout
        connection.setReadTimeout(5000);   // 5 seconds timeout

        // Set headers
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            os.write("{}".getBytes()); // Empty JSON payload
            os.flush();
        }

        // Handle the response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Successfully generated and sent candidate documents for candidate ID: " + candidateId);
        } else {
            throw new RuntimeException("Failed to generate candidate documents. HTTP Code: " + responseCode);
        }
    }
}
