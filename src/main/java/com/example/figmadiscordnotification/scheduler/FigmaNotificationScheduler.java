package com.example.figmadiscordnotification.scheduler;

import com.example.figmadiscordnotification.figma.model.domain.FigmaFile;
import com.example.figmadiscordnotification.figma.util.FigmaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FigmaNotificationScheduler {
    private FigmaFile previousFileState;
    private final FigmaUtils figmaUtils;
    @Value("${figma.fileId}")
    private String fileId;

    // 주기적으로 실행할 메서드를 설정합니다.
    @Scheduled(fixedRate = 10000) // 1분마다 실행
    public void checkFigmaUpdates() {
        // Figma API를 호출하여 현재 파일 상태를 가져옵니다.
        FigmaFile currentFileState = figmaUtils.getFile(fileId);

        // 이전 파일 상태가 없을 때 (최초 실행 시), 현재 파일 상태로 설정
        if (previousFileState == null) {
            previousFileState = currentFileState;
            return; // 이후 로직 실행하지 않고 종료
        }

        // 변경 사항 처리 후 현재 파일 상태를 이전 파일 상태로 업데이트
        previousFileState = currentFileState;
    }
}