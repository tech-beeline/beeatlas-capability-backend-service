package ru.beeline.capability.mapper;


import org.springframework.stereotype.Component;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;
import ru.beeline.fdmlib.dto.capability.PutTechCapabilityDTO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TechCapabilityMapper {
    public PutTechCapabilityDTO convert(TechCapability techCapability) {
        return PutTechCapabilityDTO.builder()
                .code(techCapability.getCode())
                .name(techCapability.getName())
                .description(techCapability.getDescription())
                .status(techCapability.getStatus())
                .author(techCapability.getAuthor())
                .link(techCapability.getLink())
                .owner(techCapability.getOwner())
                .parents(getParentsCodes(techCapability.getParents()))
                .build();
    }

    public List<String> getParentsCodes(List<TechCapabilityRelations> techCapabilitiesRelations) {
        return techCapabilitiesRelations.stream()
                .map(relation -> relation.getBusinessCapability().getCode())
                .collect(Collectors.toList());
    }
}
