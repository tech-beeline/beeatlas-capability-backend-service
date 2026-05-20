/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto.criteria;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriteriaRecordResponseDTO {

    private Long id;

    private Long criterionId;

    private Integer value;

    private Integer grade;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long bcId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long tcId;

    private String comment;
}
