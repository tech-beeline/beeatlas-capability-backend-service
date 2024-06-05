package ru.beeline.capability.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostBusinessCapabilityDTO {
    private String code;
    private String name;
    private String description;
    private String author;
    private String link;
    private String owner;
    private String status;
    private String parent;
}