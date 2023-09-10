package com.example.figmadiscordnotification.figma.util;

import com.example.figmadiscordnotification.figma.model.domain.FigmaFile;
import com.example.figmadiscordnotification.figma.model.domain.Node;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    private FigmaFile previousFileState;
    private String figmaFileId;

    public FigmaFile getFile(String fileId) {
        figmaFileId = fileId;
        String url = apiUrl + "/files/" + fileId;
        RestTemplate restTemplate = new RestTemplate();

        // Figma API 설정
        HttpHeaders figmaHeaders = new HttpHeaders();
        figmaHeaders.set("X-Figma-Token", apiToken);
        HttpEntity<String> figmaEntity = new HttpEntity<>(figmaHeaders);

        ResponseEntity<FigmaFile> figmaResponse = restTemplate.exchange(url, HttpMethod.GET, figmaEntity, FigmaFile.class);
        if (figmaResponse.getStatusCode() == HttpStatus.OK) {
            FigmaFile currentFileState = figmaResponse.getBody();

            // 이전 파일 상태가 null인 경우 초기화
            if (previousFileState == null) {
                previousFileState = currentFileState;
            }

            // 이전 파일 상태와 현재 파일 상태의 children 수 및 내용을 비교하여 변경 사항을 감지합니다.
            compareChildrenLevels(previousFileState.getDocument(), currentFileState.getDocument(), 1);

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

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDate = dateFormat.format(new Date());

            String title = "Figma 파일이 변경되었습니다!";
            String url = "https://www.figma.com/file/" + figmaFileId;
            String jsonBody = "{ \"embeds\": [{ \"title\": \"" + title + "\", \"url\": \"" + url + "\", \"description\": \"**Updates**:" + message + "\", \"color\": 16711680, \"footer\": {\"text\": \"" + currentDate + "\"} }] }";

            log.info(jsonBody);
            HttpEntity<String> discordEntity = new HttpEntity<>(jsonBody, discordHeaders);

            ClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            RestTemplate restTemplate = new RestTemplate(factory);

            ResponseEntity<String> discordResponse = restTemplate.exchange(discordWebhookUrl, HttpMethod.POST, discordEntity, String.class);

            if (discordResponse.getStatusCode() != HttpStatus.OK) {
//                throw new RuntimeException("디스코드 웹훅 전송을 실패하였습니다.");
            }
        } catch (Exception e) {
            throw new RuntimeException("디스코드 웹훅 전송을 실패하였습니다. \n", e);
        }
    }

    public void compareChildrenLevels(Node previousNode, Node currentNode, int level) {
        if (previousNode == null || currentNode == null) {
            return;
        }

        String levelPrefix = "".repeat(level);

        int previousChildrenCount = previousNode.getChildren() != null ? previousNode.getChildren().size() : 0;
        int currentChildrenCount = currentNode.getChildren() != null ? currentNode.getChildren().size() : 0;

        // 2번째 뎁스 변경 사항 검사
        if (level == 1) {
            int childrenDiff = currentChildrenCount - previousChildrenCount;
            String message = "";
            if (childrenDiff > 0) {
                message += "\\n✅ " + levelPrefix + " 2번째 뎁스 " + childrenDiff + "개 추가";
            } else if (childrenDiff < 0) {
                message += "\\n❌ " + levelPrefix + " 2번째 뎁스 " +  (-childrenDiff) + "개 삭제";
            }
            if (childrenDiff != 0) { // 변경이 있을 때만 로그 출력
//                log.info(message);
                sendNotificationToDiscord(message);
            }
        }

        // 3번째 뎁스 비교
        if (previousNode.getChildren() != null && currentNode.getChildren() != null) {
            int previousThirdLevelChildrenCount = previousNode.getChildren().size();
            int currentThirdLevelChildrenCount = currentNode.getChildren().size();
            int thirdLevelChildrenDiff = currentThirdLevelChildrenCount - previousThirdLevelChildrenCount;

            if (level == 2 && thirdLevelChildrenDiff != 0) {
                String message = "\\n:pencil2:" + currentNode.getName() + "의 " + levelPrefix + "3번째 뎁스의 변경된 요소 개수: " + thirdLevelChildrenDiff;
//                log.info(message);
                sendNotificationToDiscord(message);
            }

            for (int i = 0; i < Math.min(previousChildrenCount, currentChildrenCount); i++) {
                Node previousThirdLevelNode = previousNode.getChildren().get(i);
                Node currentThirdLevelNode = currentNode.getChildren().get(i);

                // 재귀돌려서 더 깊은 뎁스 비교
                compareChildrenLevels(previousThirdLevelNode, currentThirdLevelNode, level + 1);
            }
        }
    }

}
