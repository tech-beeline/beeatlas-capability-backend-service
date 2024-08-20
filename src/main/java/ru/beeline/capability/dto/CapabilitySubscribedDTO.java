package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CapabilitySubscribedDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isDomain;
    private String owner;
}
