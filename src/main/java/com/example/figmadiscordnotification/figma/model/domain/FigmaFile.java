package com.example.figmadiscordnotification.figma.model.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class FigmaFile {
    private String name;
    private String role;
    private String lastModified;
    private String editorType;
    private String thumbnailUrl;
    private String version;
    private Node document;
    private Map<String, Component> components;
    private Map<String, ComponentSet> componentSets;
    private int schemaVersion;
    private Map<String, Style> styles;
    private String mainFileKey;
    private List<Branch> branches;

    public static class Component {
        // Component에 대한 필드 정의
    }

    public static class ComponentSet {
        // ComponentSet에 대한 필드 정의
    }

    public static class Style {
        // Style에 대한 필드 정의
    }

    public static class Branch {
        private String key;
        private String name;
        private String thumbnailUrl;
        private String lastModified;
        private String linkAccess;
    }
}