package com.synhrgy.recruitement;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.xml.soap.*;
import java.net.URL;

public class OnboardCandidateDelegate implements JavaDelegate {
    private static final String SOAP_API_URL = System.getenv("ONBOARDING_SERVICE_URL") != null
            ? System.getenv("ONBOARDING_SERVICE_URL")
            : "http://127.0.0.1:8000";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Fetch the employee ID from process variables
        String employeeId = (String) execution.getVariable("employeeId");

        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is missing from process variables.");
        }

        // Call the SOAP API
        String response = callOnboardingService(employeeId);

        // Log the response for debugging
        System.out.println("Onboarding response: " + response);

        // Optionally, you can set the response as a process variable
        execution.setVariable("onboardingResponse", response);
    }

    private String callOnboardingService(String employeeId) throws Exception {
        // Create a SOAP Connection
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

        // Construct the SOAP message
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        // Set up the SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("tns", "http://example.com/onboarding");

        // Create the SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement onboardCandidateRequest = soapBody.addChildElement("onboard_candidate", "tns");
        SOAPElement employeeIdElement = onboardCandidateRequest.addChildElement("employee_id", "tns");
        employeeIdElement.addTextNode(employeeId);

        // Save changes to the SOAP message
        soapMessage.saveChanges();

        // Print the SOAP request (optional)
        System.out.println("SOAP Request:");
        soapMessage.writeTo(System.out);
        System.out.println();

        // Send the request to the SOAP API and get the response
        URL endpoint = new URL(SOAP_API_URL);
        SOAPMessage soapResponse = soapConnection.call(soapMessage, endpoint);

        // Close the SOAP connection
        soapConnection.close();

        // Parse the SOAP Response
        SOAPBody responseBody = soapResponse.getSOAPBody();
        if (responseBody.hasFault()) {
            throw new RuntimeException("SOAP Fault: " + responseBody.getFault().getFaultString());
        }

        // Extract the response content
        return responseBody.getTextContent();
    }
}
