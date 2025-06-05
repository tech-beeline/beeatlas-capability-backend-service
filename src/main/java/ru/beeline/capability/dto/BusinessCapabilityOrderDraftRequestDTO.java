package ru.beeline.capability.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityOrderDraftRequestDTO {

    private String name;
    private String description;
    private String owner;
    private Integer parentId;
    private Long mutableBcId;

}
