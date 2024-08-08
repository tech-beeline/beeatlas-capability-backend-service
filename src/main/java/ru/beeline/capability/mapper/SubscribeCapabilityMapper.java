package ru.beeline.capability.mapper;

import org.springframework.stereotype.Component;
import ru.beeline.capability.domain.TechCapability;
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
}