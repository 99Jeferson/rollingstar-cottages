package com.rollingstar.cottages.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class MobileMoneyService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    // Flutterwave Charge Endpoint for East African Mobile Money Wallets
    private final String GATEWAY_API_URL = "https://api.flutterwave.com/v3/charges?type=mobile_money_uganda";
    
    // Replace this string placeholder with your actual Secret Key from the Flutterwave Dashboard
    private final String SECRET_KEY = "FLWSECK_YOUR_TEST_OR_LIVE_KEY_HERE"; 

    /**
     * Executes the outgoing API request to trigger the Mobile Money payment prompt (STK Push).
     * Marked @Async so the front desk screen doesn't lag while waiting for telecom signals.
     */
    @Async
    public void triggerPushPrompt(String phoneNumber, double amount, String cottageCode) {
        try {
            // Build raw JSON payload matching Flutterwave's Uganda Mobile Money specification
            String jsonPayload = """
                {
                    "amount": "%s",
                    "currency": "UGX",
                    "phone_number": "%s",
                    "network": "%s",
                    "email": "billing@rollingstar.com",
                    "tx_ref": "RS-PAY-%s-%s",
                    "redirect_url": "https://rollingstar.com/dashboard"
                }
                """.formatted(
                    String.valueOf((int)amount), 
                    phoneNumber, 
                    detectNetworkOperator(phoneNumber), 
                    cottageCode, 
                    String.valueOf(System.currentTimeMillis())
                );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GATEWAY_API_URL))
                    .header("Authorization", "Bearer " + SECRET_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Fire off the background API request dispatch loop
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println(" TELECOM GATEWAY API RESPONSE: " + response.body());

        } catch (Exception e) {
            System.err.println(" CRITICAL SERVICE ERROR: Failed calling mobile wallet API gateway: " + e.getMessage());
        }
    }

    /**
     * Determines whether the number belongs to MTN or Airtel to pass the correct parameter to the switch.
     */
    private String detectNetworkOperator(String sanitizedPhone) {
        if (sanitizedPhone.startsWith("25677") || sanitizedPhone.startsWith("25678") || sanitizedPhone.startsWith("25676") || sanitizedPhone.startsWith("25679")) {
            return "MTN";
        } else if (sanitizedPhone.startsWith("25670") || sanitizedPhone.startsWith("25675") || sanitizedPhone.startsWith("25674")){
            return "AIRTEL";
        }
        return "UGANDA_TELECOM";
    }
}