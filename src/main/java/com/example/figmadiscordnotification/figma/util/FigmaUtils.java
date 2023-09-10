package com.example.figmadiscordnotification.figma.util;

import com.example.figmadiscordnotification.figma.model.domain.FigmaFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class FigmaUtils {
    @Value("${figma.apiUrl}")
    private String apiUrl;
    @Value("${figma.apiToken}")
    private String apiToken;
    @Value("${discord.webhookUrl}")
    private String discordWebhookUrl;
    private FigmaFile previousFileState; // 이전 상태를 저장하는 필드

    public FigmaFile getFile(String fileId) {
        String url = apiUrl + "/files/" + fileId;
        RestTemplate restTemplate = new RestTemplate();

        // Set API token in the headers for Figma API request
        HttpHeaders figmaHeaders = new HttpHeaders();
        figmaHeaders.set("X-Figma-Token", apiToken);
        HttpEntity<String> figmaEntity = new HttpEntity<>(figmaHeaders);

        // Make the GET request to Figma API and parse the JSON response
        ResponseEntity<FigmaFile> figmaResponse = restTemplate.exchange(url, HttpMethod.GET, figmaEntity, FigmaFile.class);
        if (figmaResponse.getStatusCode() == HttpStatus.OK) {
            FigmaFile currentFileState = figmaResponse.getBody();

            // 이전 파일 상태와 현재 파일 상태를 비교하여 변경 사항을 감지합니다.
            boolean isUpdateDetected = detectUpdates(previousFileState, currentFileState);

            // 변경 사항이 있으면 Discord Webhook을 사용하여 알림을 보냅니다.
            if (isUpdateDetected) {
//                sendNotificationToDiscord("Figma 파일이 수정되었습니다.");
                log.warn("이게 보내지네?");
            }

            // 현재 파일 상태를 이전 파일 상태로 업데이트합니다.
            previousFileState = currentFileState;

            return currentFileState;
        } else {
            throw new RuntimeException("Failed to fetch Figma file");
        }
    }

    private void sendNotificationToDiscord(String message) {
        try {
            HttpHeaders discordHeaders = new HttpHeaders();
            discordHeaders.setContentType(MediaType.APPLICATION_JSON);

            String jsonBody = "{ \"content\": \"" + message + "\" }";

            HttpEntity<String> discordEntity = new HttpEntity<>(jsonBody, discordHeaders);

            // Create a RestTemplate with a simple request factory
            ClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            RestTemplate restTemplate = new RestTemplate(factory);

            // Make a POST request to the Discord Webhook URL
            ResponseEntity<String> discordResponse = restTemplate.exchange(discordWebhookUrl, HttpMethod.POST, discordEntity, String.class);

            if (discordResponse.getStatusCode() != HttpStatus.OK) {
//                throw new RuntimeException("Failed to send message to Discord Webhook");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to Discord Webhook", e);
        }
    }

    private boolean detectUpdates(FigmaFile previousState, FigmaFile currentState) {
        if (previousState == null) {
            // 이전 상태가 null인 경우 변경이 감지되지 않은 것으로 처리합니다.
            return false;
        }

        // 이전 상태와 현재 상태의 lastModified 값을 비교합니다.
        String previousLastModified = previousState.getLastModified();
        String currentLastModified = currentState.getLastModified();

        // lastModified 값이 변경되었는지 확인합니다.
        boolean lastModifiedChanged = !Objects.equals(previousLastModified, currentLastModified);

        // lastModified 값이 변경되었으면 true를 반환합니다.
        return lastModifiedChanged;
    }

}
