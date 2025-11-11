package com.temaServlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletB extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        response.setHeader("Access-Control-Allow-Methods", "POST");

        // extract data from frontend req
        String inputText = request.getParameter("inputText");
        String operation = request.getParameter("operation");

        System.out.println("[ServletB] Received data: text='" + inputText + "', op='" + operation + "'");

        String result = "";

        // process data
        if (inputText == null || operation == null) {
            result = "Error: Missing data.";
        } else {
            switch (operation) {
                case "reverse":
                    result = new StringBuilder(inputText).reverse().toString();
                    break;
                case "uppercase":
                    result = inputText.toUpperCase();
                    break;
                case "count":
                    result = String.valueOf(inputText.trim().split("\\s+").length) + " words";
                    break;
                default:
                    result = "Error: Unknown operation '" + operation + "'";
            }
        }

        System.out.println("[ServletB] Sending result: '" + result + "'");

        // send result
        response.setContentType("text/plain");
        response.getWriter().write(result);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        resp.setHeader("Access-Control-Allow-Methods", "POST");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}