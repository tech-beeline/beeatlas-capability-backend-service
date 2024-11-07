package ru.beeline.capability.mapper;

import org.springframework.stereotype.Component;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.dto.BusinessCapabilitySubscribedDTO;
import ru.beeline.capability.dto.CapabilitySubscribedDTO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubscribeCapabilityMapper {
    public List<CapabilitySubscribedDTO> convert(List<TechCapability> techCapabilities) {
        return techCapabilities.stream().map(this::convert).collect(Collectors.toList());
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
        return businessCapabilities.stream().map(this::convertToCapabilitySubscribedDTO).collect(Collectors.toList());
    }

    public BusinessCapabilitySubscribedDTO convertToCapabilitySubscribedDTO(BusinessCapability businessCapabilities) {
        return new BusinessCapabilitySubscribedDTO(
                businessCapabilities.getParentId(),
                businessCapabilities.getId(),
                businessCapabilities.getCode(),
                businessCapabilities.getName(),
                businessCapabilities.getDescription(),
                businessCapabilities.isDomain(),
                businessCapabilities.getOwner()
        );
    }


}