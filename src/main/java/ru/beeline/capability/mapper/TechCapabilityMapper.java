package ru.beeline.capability.mapper;


import org.springframework.stereotype.Component;
import ru.beeline.capability.domain.HistoryTechCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;
import ru.beeline.capability.dto.HistoryTechCapabilityDTO;
import ru.beeline.capability.dto.ParentDTO;
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
                .owner(techCapability.getOwner())
                .author(techCapability.getAuthor())
                .link(techCapability.getLink())
                .createdDate(techCapability.getCreatedDate())
                .lastModifiedDate(techCapability.getLastModifiedDate())
                .build();
    }

    public HistoryTechCapabilityDTO toHistoryTechCapabilityDTO(HistoryTechCapability historyTechCapability, List<ParentDTO> parentDTOS, Long id, Integer version) {
        return HistoryTechCapabilityDTO.builder()
                .id(id)
                .code(historyTechCapability.getCode())
                .name(historyTechCapability.getName())
                .description(historyTechCapability.getDescription())
                .owner(historyTechCapability.getOwner())
                .modifiedDate(historyTechCapability.getModifiedDate())
                .deletedDate(historyTechCapability.getDeletedDate())
                .status(historyTechCapability.getStatus())
                .author(historyTechCapability.getAuthor())
                .link(historyTechCapability.getLink())
                .version(version)
                .parents(parentDTOS)
                .build();
    }

    public HistoryTechCapabilityDTO toHistoryTechCapabilityDTO(TechCapability techCapability, List<ParentDTO> parentDTOS, Long id, Integer version) {
        return HistoryTechCapabilityDTO.builder()
                .id(id)
                .code(techCapability.getCode())
                .name(techCapability.getName())
                .description(techCapability.getDescription())
                .owner(techCapability.getOwner())
                .modifiedDate(techCapability.getLastModifiedDate())
                .deletedDate(techCapability.getDeletedDate())
                .status(techCapability.getStatus())
                .author(techCapability.getAuthor())
                .link(techCapability.getLink())
                .version(version)
                .parents(parentDTOS)
                .build();
    }
}
