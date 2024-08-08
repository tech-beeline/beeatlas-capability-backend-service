package ru.beeline.capability.mapper;


import org.springframework.stereotype.Component;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;
import ru.beeline.fdmlib.dto.capability.PutTechCapabilityDTO;
import ru.beeline.fdmlib.dto.capability.TechCapabilityShortDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TechCapabilityMapper {
    public PutTechCapabilityDTO convertToPutTechCapabilityDTO(TechCapability techCapability) {
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


    public static List<TechCapabilityShortDTO> convertToTechCapabilityShortDTOList(List<TechCapability> techCapabilities) {
        List<TechCapabilityShortDTO> techCapabilityDTOS = new ArrayList<>();
        for (TechCapability techCapability : techCapabilities) {
            TechCapabilityShortDTO techCapabilityDTO = convertToTechCapabilityShortDTO(techCapability);
            techCapabilityDTOS.add(techCapabilityDTO);
        }
        techCapabilityDTOS.sort(Comparator.comparing(TechCapabilityShortDTO::getName));
        return techCapabilityDTOS;
    }

    public static TechCapabilityShortDTO convertToTechCapabilityShortDTO(TechCapability techCapability) {
        return TechCapabilityShortDTO.builder()
                .id(techCapability.getId())
                .code(techCapability.getCode())
                .name(techCapability.getName())
                .description(techCapability.getDescription())
                .author(techCapability.getAuthor())
                .link(techCapability.getLink())
                .createdDate(techCapability.getCreatedDate())
                .lastModifiedDate(techCapability.getLastModifiedDate())
                .build();
    }
}
