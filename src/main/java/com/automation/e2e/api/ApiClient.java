package com.automation.e2e.api;

import com.microsoft.playwright.APIRequest; // Ensure this import is added
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import java.util.HashMap;
import java.util.Map;

public class ApiClient {
    private final APIRequestContext requestContext;
    private final String baseUrl = "https://billpay-api.gauravkhurana-practice-api.workers.dev";
    private final String apiKey = "demo-api-key-123";

    public ApiClient(Playwright playwright) {
        Map<String, String> defaultHeaders = new HashMap<>();
        // FIX 1: Change .set() to .put()
        defaultHeaders.put("X-API-Key", apiKey);

        // FIX 2: Explicitly reference APIRequest.NewContextOptions
        this.requestContext = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(baseUrl)
                        .setExtraHTTPHeaders(defaultHeaders)
        );
    }

    public APIResponse post(String endpoint, String payload, String contentType) {
        return requestContext.post(endpoint,
                RequestOptions.create()
                        .setHeader("Content-Type", contentType)
                        .setHeader("Accept", contentType)
                        .setData(payload)
        );
    }

    public APIResponse get(String endpoint, String acceptType) {
        return requestContext.get(endpoint,
                RequestOptions.create()
                        .setHeader("Accept", acceptType)
        );
    }

    public void dispose() {
        if (requestContext != null) {
            requestContext.dispose();
        }
    }
}