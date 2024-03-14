package ru.beeline.capability.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityDTOTemp {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String status;
    private String author;
    private String link;
    private String owner;
    private String parents;
    private Boolean isDomain;
}
