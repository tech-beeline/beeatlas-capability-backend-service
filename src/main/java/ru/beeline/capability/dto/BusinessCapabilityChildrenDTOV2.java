/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityChildrenDTOV2 {
    private List<TechCapabilityShortDTOV2> techCapabilities;
    private List<BusinessCapabilityDTO> businessCapabilities;
}
