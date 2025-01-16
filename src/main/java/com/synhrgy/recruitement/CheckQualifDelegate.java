package com.synhrgy.recruitement;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckQualifDelegate implements JavaDelegate {

    private static final String RECRUITMENT_SERVICE_URL = "http://localhost:4003";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Get candidateId from process variables
        String candidateId = (String) execution.getVariable("applicationId");

        // Validate candidateId
        if (candidateId == null || candidateId.isEmpty()) {
            throw new IllegalArgumentException("applicationId must not be null or empty");
        }

        // Construct the URL for the GET request
        URL url = new URL(RECRUITMENT_SERVICE_URL + "/checkQualifications/" + candidateId);

        // Open the connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");

        // Check the response code
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse the response as JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            boolean meetsQualifications = jsonResponse.optBoolean("meetsQualifications", false);
            

            // Set the result in process variables
            execution.setVariable("meetsQualifications", meetsQualifications);
        } else {
            // Handle non-200 responses
            throw new RuntimeException("Failed to fetch qualification status: HTTP " + responseCode);
        }
    }

}
