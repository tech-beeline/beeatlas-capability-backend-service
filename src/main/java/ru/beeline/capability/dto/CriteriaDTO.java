package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriteriaDTO {

    @JsonProperty("criteria_id")
    private Integer criteriaId;
    private Integer value;
    private Integer grade;
    private String comment;
}