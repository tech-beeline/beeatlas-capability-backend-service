package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityCriteriaDTO {
    @JsonProperty("criterion_id")
    private Long criterionId;

    private Integer value;

    private Integer grade;
}
