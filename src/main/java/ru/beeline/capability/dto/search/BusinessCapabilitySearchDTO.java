/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilitySearchDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean isDomain;
    private List<CapabilityDomainDTO> parents;
}
