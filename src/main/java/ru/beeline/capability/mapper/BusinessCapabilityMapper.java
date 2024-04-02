package ru.beeline.capability.mapper;

import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BusinessCapabilityMapper {
    public static List<BusinessCapabilityDTO> convert(List<BusinessCapability> businessCapabilities) {
        List<BusinessCapabilityDTO> businessCapabilityDTOS = new ArrayList<>();
        for (BusinessCapability businessCapability : businessCapabilities) {
            BusinessCapabilityDTO businessCapabilityDTO = convert(businessCapability);
            businessCapabilityDTOS.add(businessCapabilityDTO);
        }
        businessCapabilityDTOS.sort(Comparator.comparing(BusinessCapabilityDTO::getName));
        return businessCapabilityDTOS;
    }

    public static BusinessCapabilityDTO convert(BusinessCapability techCapability) {
        return BusinessCapabilityDTO.builder()
                .id(techCapability.getId())
                .code(techCapability.getCode())
                .name(techCapability.getName())
                .description(techCapability.getDescription())
                .author(techCapability.getAuthor())
                .link(techCapability.getLink())
                .createdDate(techCapability.getCreatedDate())
                .hasChildren(!techCapability.getChildren().isEmpty())
                .build();
    }
}