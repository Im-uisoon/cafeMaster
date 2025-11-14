package com.project.summer.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference; // 추가
import com.project.summer.exception.ErrorResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class ApiClient {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private String jwtToken;

    public ApiClient() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    private <T> T sendRequest(HttpRequest request, Class<T> responseType) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (responseType == Void.class) {
                return null;
            }
            // 응답 바디가 비어있을 경우 null 반환
            if (response.body() == null || response.body().isEmpty()) {
                return null;
            }
            return objectMapper.readValue(response.body(), responseType);
        } else {
            // 에러 응답 바디가 비어있을 경우 기본 메시지 사용
            String errorMessage = "알 수 없는 오류가 발생했습니다.";
            if (response.body() != null && !response.body().isEmpty()) {
                try {
                    ErrorResponse errorResponse = objectMapper.readValue(response.body(), ErrorResponse.class);
                    errorMessage = errorResponse.getMessage();
                } catch (Exception e) {
                    // JSON 파싱 실패 시 원시 응답 바디 사용
                    errorMessage = response.body();
                }
            }
            throw new ApiException(errorMessage);
        }
    }

    private <T> T sendRequest(HttpRequest request, TypeReference<T> responseType) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (responseType.getType().equals(Void.class)) {
                return null;
            }
            if (response.body() == null || response.body().isEmpty()) {
                return null;
            }
            return objectMapper.readValue(response.body(), responseType);
        } else {
            String errorMessage = "알 수 없는 오류가 발생했습니다.";
            if (response.body() != null && !response.body().isEmpty()) {
                try {
                    ErrorResponse errorResponse = objectMapper.readValue(response.body(), ErrorResponse.class);
                    errorMessage = errorResponse.getMessage();
                } catch (Exception e) {
                    errorMessage = response.body();
                }
            }
            throw new ApiException(errorMessage);
        }
    }

    public <T> T get(String url, Class<T> responseType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();
        return sendRequest(request, responseType);
    }

    public <T> T get(String url, TypeReference<T> responseType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();
        return sendRequest(request, responseType);
    }

    public <T> T post(String url, Object body, Class<T> responseType) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        return sendRequest(request, responseType);
    }

    public <T> T put(String url, Object body, Class<T> responseType) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        return sendRequest(request, responseType);
    }

    public void delete(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + jwtToken)
                .DELETE()
                .build();
        sendRequest(request, Void.class);
    }
}
