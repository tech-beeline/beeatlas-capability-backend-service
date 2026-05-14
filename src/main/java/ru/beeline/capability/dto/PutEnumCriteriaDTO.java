/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PutEnumCriteriaDTO {

    private String name;
    private String description;
    private String type;
    private Integer interval;
    private Integer threshold;
    private Boolean revers;
    private String minDesc;
    private String maxDesc;
}
