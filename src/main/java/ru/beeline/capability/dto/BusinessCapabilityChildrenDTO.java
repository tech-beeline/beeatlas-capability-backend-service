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
public class BusinessCapabilityChildrenDTO {
    private List<TechCapabilityShortDTO> techCapabilities;
    private List<BusinessCapabilityDTO> businessCapabilities;
}
