package ru.beeline.capability.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTechCapabilityDTO {
    private String code;
    private String name;
    private String description;
    private String author;
    private String link;
    private String owner;
    private List<String> parents;
    private String status;
}
