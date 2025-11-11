package com.temaServlet;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebServlet("/servletA")
public class ServletA extends HttpServlet {

    private static final String BACKEND_URL = "http://localhost:9090/backend-app/servletB"; //port 9090 cred

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // gets 
        String inputText = request.getParameter("inputText");
        String operation = request.getParameter("operation");

        System.out.println("[ServletA] Received data: text='" + inputText + "', op='" + operation + "'");
        System.out.println("[ServletA] Calling backend service at: " + BACKEND_URL);

        try {
            // prep data
            String postData = "inputText=" + java.net.URLEncoder.encode(inputText, StandardCharsets.UTF_8) +
                              "&operation=" + java.net.URLEncoder.encode(operation, StandardCharsets.UTF_8);

            // build request
            HttpClient client = HttpClient.newHttpClient();
            
            HttpRequest backendRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL))
                    .POST(HttpRequest.BodyPublishers.ofString(postData))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            // send req
            HttpResponse<String> backendResponse = client.send(backendRequest, HttpResponse.BodyHandlers.ofString());

            String resultFromBackend = backendResponse.body();
            System.out.println("[ServletA] Received response from backend: '" + resultFromBackend + "'");

            // send response to user
            response.setContentType("text/plain");
            response.getWriter().write(resultFromBackend);

        } catch (Exception e) {
            System.err.println("[ServletA] Error calling backend: " + e.getMessage());
            e.printStackTrace();
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error: Could not connect to the backend service. Is it running?");
        }
    }
}