package com.example.figmadiscordnotification.figma.model.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FigmaNode {
    private String id;
    private String name;
    private String type; // 노드 유형 (예: "RECTANGLE", "TEXT", "IMAGE")
    private String imageUrl; // 이미지 노드의 경우 이미지 URL
    private String textContent; // 텍스트 노드의 경우 텍스트 내용

    public FigmaNode(String id, String name, String type, String imageUrl, String textContent) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.imageUrl = imageUrl;
        this.textContent = textContent;
    }
}
