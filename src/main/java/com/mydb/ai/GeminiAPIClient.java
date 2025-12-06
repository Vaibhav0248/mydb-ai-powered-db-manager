package com.mydb.ai;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GeminiAPIClient {

    private static String API_KEY = "";
    private final Gson gson = new Gson();
    private HttpRequest.Builder requestBuilder;

    // ----------------------------
    //  CONFIG
    // ----------------------------
    public static void setApiKey(String key) {
        API_KEY = key;
    }

    public static String getApiKey() {
        return API_KEY;
    }

    public static boolean isConfigured() {
        return API_KEY != null && !API_KEY.isEmpty();
    }

    private void initializeRequestBuilder() {
        if (!isConfigured()) {
            throw new IllegalStateException("API Key not configured");
        }

        try {
            // CORRECT 2025 API: v1 endpoint with gemini-2.5-flash
            URI uri = new URI(
                "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY
            );

            requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30));

        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid Gemini API URL", e);
        }
    }

    // ----------------------------
    //   PUBLIC METHOD
    // ----------------------------
    public String generateSQLQuery(String naturalLanguage, String databaseSchema) throws IOException {
        initializeRequestBuilder();

        String prompt = buildPrompt(naturalLanguage, databaseSchema);
        GeminiPrompt promptObj = new GeminiPrompt(prompt);
        String jsonPayload = gson.toJson(promptObj);

        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<String> response;

        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }

        // Better error logging
        if (response.statusCode() != 200) {
            String errorMsg = "Gemini API Error (" + response.statusCode() + "):\n" + response.body();
            System.err.println(errorMsg);
            throw new IOException(errorMsg);
        }

        return parseResponseSQL(response.body());
    }

    // ----------------------------
    //  STRICT PROMPT
    // ----------------------------
    private String buildPrompt(String naturalLanguage, String databaseSchema) {
        return "You are an expert SQL generator. Convert the following natural language "
                + "request into a VALID MySQL query.\n\n"

                + "DATABASE SCHEMA:\n"
                + databaseSchema + "\n\n"

                + "USER REQUEST:\n"
                + naturalLanguage + "\n\n"

                + "STRICT RULES:\n"
                + "- Return ONLY the SQL query\n"
                + "- No explanations\n"
                + "- No markdown\n"
                + "- No code blocks\n"
                + "- No backticks\n"
                + "- Use exact table & column names from schema\n"
                + "- Output only 1 SQL statement\n\n"

                + "SQL QUERY:";
    }

    // ----------------------------
    //   PARSE CLEAN SQL
    // ----------------------------
    private String parseResponseSQL(String jsonResponse) {
        try {
            GeminiResponse res = gson.fromJson(jsonResponse, GeminiResponse.class);

            String sql = res.candidates.get(0).content.parts.get(0).text;

            // Clean response
            sql = sql.trim()
                    .replace("```sql", "")
                    .replace("```", "")
                    .trim();

            return sql;

        } catch (Exception e) {
            System.err.println("Parse error. Response was: " + jsonResponse);
            throw new RuntimeException(
                    "Failed to parse SQL from Gemini response.\n" +
                    "Response was:\n" + jsonResponse, e
            );
        }
    }

    // ----------------------------
    //  OPTIONAL: QUERY OPTIMIZER
    // ----------------------------
    public String optimizeQuery(String sqlQuery, String databaseSchema) throws IOException {
        initializeRequestBuilder();

        String prompt =
                "You are a SQL optimization expert. Optimize the following MySQL query.\n\n"
                + "DATABASE SCHEMA:\n" + databaseSchema + "\n\n"
                + "ORIGINAL QUERY:\n" + sqlQuery + "\n\n"
                + "Return:\n"
                + "1. Optimized query\n"
                + "2. Explanation\n"
                + "3. Index recommendations\n";

        GeminiPrompt promptObj = new GeminiPrompt(prompt);
        String jsonPayload = gson.toJson(promptObj);

        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<String> response;

        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }

        if (response.statusCode() != 200) {
            String errorMsg = "Gemini API Error: " + response.statusCode() + "\n" + response.body();
            System.err.println(errorMsg);
            throw new IOException(errorMsg);
        }

        return parseResponseSQL(response.body());
    }
}
