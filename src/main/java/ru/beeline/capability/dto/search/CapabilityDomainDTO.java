package ru.beeline.capability.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapabilityDomainDTO {

    private Long id;
    private String code;
    private String name;
    private boolean isDomain;
}

