package com.synhrgy.recruitement;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.xml.soap.*;
import java.net.URL;

public class OnboardCandidateDelegate implements JavaDelegate {
    private static final String SOAP_API_URL = "http://localhost:4004";


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Fetch the employee ID from process variables
        String employeeId = (String) execution.getVariable("applicationId");

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
        // Print the SOAP response
        System.out.println("SOAP Response:");
        soapResponse.writeTo(System.out);
        System.out.println();        
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

    public static void main(String[] args) {
        OnboardCandidateDelegate delegate = new OnboardCandidateDelegate();
        try {
            delegate.callOnboardingService("12");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
