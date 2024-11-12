package ru.beeline.capability.utils;

import lombok.Data;
import ru.beeline.capability.domain.BusinessCapability;

import java.util.ArrayList;
import java.util.List;

@Data
public class Node {
    private Long id;
    private Long parentId;
    private List<Node> children;
    private Long countTech = 0L;
    private Long value = 0L;
    private Long grade = 0L;
    private BusinessCapability businessCapability;

    public Node(Long id, Long parentId) {
        this.id = id;
        this.parentId = parentId;
        this.children = new ArrayList<>();
    }

    public Node(Long id, Long parentId, BusinessCapability businessCapability) {
        this.id = id;
        this.parentId = parentId;
        this.children = new ArrayList<>();
        this.businessCapability = businessCapability;
    }


}
