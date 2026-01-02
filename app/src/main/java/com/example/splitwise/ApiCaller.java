package com.example.splitwise;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP client for making API calls to the backend server
 * Provides three methods for different return types: String[][], String, and String[]
 */
public class ApiCaller {

    /**
     * Makes HTTP GET request and returns parsed 2D string array
     */
    public static String[][] ApiCaller1(String urlString) {
        String response = makeHttpRequest(urlString);
        return parseJsonToArray(response);
    }

    /**
     * Makes HTTP GET request and returns raw string response
     */
    public static String ApiCaller2(String urlString) {
        return makeHttpRequest(urlString);
    }

    /**
     * Makes HTTP GET request and returns parsed string array
     */
    public static String[] ApiCaller3(String urlString) {
        String response = makeHttpRequest(urlString);
        return parseJsonToStringArray(response);
    }

    /**
     * Core HTTP connection method for Android with proper URL encoding
     */
    private static String makeHttpRequest(String urlString) {
        StringBuilder result = new StringBuilder();
        try {
            // Fix spaces in URLs for Android
            URL url = new URL(urlString.replace(" ", "%20"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            if (conn.getResponseCode() == 200) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                return result.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ""; // Return empty string on error to avoid null pointers
    }

    /**
     * Parses JSON response into 2D string array
     */
    private static String[][] parseJsonToArray(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("")) return new String[0][0];
        json = json.trim();
        if (json.startsWith("[") && json.endsWith("]")) json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return new String[0][0];

        List<String[]> rows = new ArrayList<>();
        String[] rowStrings = json.split("\\}\\s*,\\s*\\{|\\]\\s*,\\s*\\[");
        for (String rowStr : rowStrings) {
            rowStr = rowStr.trim();
            if (rowStr.startsWith("{") || rowStr.startsWith("[")) rowStr = rowStr.substring(1);
            if (rowStr.endsWith("}") || rowStr.endsWith("]")) rowStr = rowStr.substring(0, rowStr.length() - 1);
            List<String> values = new ArrayList<>();
            String[] parts = rowStr.split(",");
            for (String part : parts) {
                part = part.trim();
                int colonIndex = part.indexOf(':');
                if (colonIndex > 0) part = part.substring(colonIndex + 1).trim();
                part = part.replaceAll("^\"|\"$", "");
                values.add(part);
            }
            rows.add(values.toArray(new String[0]));
        }
        return rows.toArray(new String[0][0]);
    }

    /**
     * Parses JSON response into string array
     */
    private static String[] parseJsonToStringArray(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("")) return new String[0];
        json = json.trim();
        if (json.startsWith("[") && json.endsWith("]")) json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return new String[0];

        List<String> values = new ArrayList<>();
        String[] parts = json.split(",");
        for (String part : parts) {
            part = part.trim().replaceAll("^\"|\"$", "");
            values.add(part);
        }
        return values.toArray(new String[0]);
    }
}