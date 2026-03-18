/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

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
    private Long mutableBcId;
    private String comment;
}
