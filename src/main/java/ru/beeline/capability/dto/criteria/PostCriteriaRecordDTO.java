/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto.criteria;

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
public class PostCriteriaRecordDTO {

    private String type;
    private String code;
    private String criterionName;
    private Integer value;
    private Integer grade;
    private String comment;
}
