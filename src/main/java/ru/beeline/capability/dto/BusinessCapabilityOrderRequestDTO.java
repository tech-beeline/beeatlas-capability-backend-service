package ru.beeline.capability.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityOrderRequestDTO {

    private String name;
    private String description;
    private String owner;
    private Integer parentId;
    private String author;
    private Long mutableBcId;
    private String comment;

}
