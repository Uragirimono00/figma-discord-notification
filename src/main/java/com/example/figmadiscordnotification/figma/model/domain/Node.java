package com.example.figmadiscordnotification.figma.model.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Node {
    private String id;
    private String name;
    private String type;
    private String scrollBehavior;
    private List<Node> children;
}
