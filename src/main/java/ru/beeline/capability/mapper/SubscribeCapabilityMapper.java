/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.mapper;

import org.springframework.stereotype.Component;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.dto.BusinessCapabilitySubscribedDTO;
import ru.beeline.capability.dto.CapabilitySubscribedDTO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubscribeCapabilityMapper {
    public List<CapabilitySubscribedDTO> convert(List<TechCapability> techCapabilities) {
        return techCapabilities.parallelStream()
                .map(this::convert)
                .sorted(Comparator.comparing(CapabilitySubscribedDTO::getId))
                .collect(Collectors.toList());
    }

    public CapabilitySubscribedDTO convert(TechCapability techCapability) {
        return CapabilitySubscribedDTO.builder()
                .id(techCapability.getId())
                .code(techCapability.getCode())
                .name(techCapability.getName())
                .description(techCapability.getDescription())
                .owner(techCapability.getOwner())
                .build();
    }

    public List<CapabilitySubscribedDTO> convertToCapabilitySubscribedDTOs(List<BusinessCapability> businessCapabilities) {
        return businessCapabilities.parallelStream()
                .map(this::convertToCapabilitySubscribedDTO)
                .sorted(Comparator.comparing(CapabilitySubscribedDTO::getId))
                .collect(Collectors.toList());
    }

    public BusinessCapabilitySubscribedDTO convertToCapabilitySubscribedDTO(BusinessCapability businessCapability) {
        return new BusinessCapabilitySubscribedDTO(
                businessCapability.getParentId(),
                businessCapability.getId(),
                businessCapability.getCode(),
                businessCapability.getName(),
                businessCapability.getDescription(),
                businessCapability.isDomain(),
                businessCapability.getOwner()
        );
    }
}