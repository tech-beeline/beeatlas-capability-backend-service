package ru.beeline.capability.dto;

import lombok.*;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapabilityDTO {
    private TechCapabilityShortDTO tech;
    private Long owner;
    private List<BusinessCapabilityDTO> parents;

    public static CapabilityDTO convert(TechCapability techCapability) {
        TechCapabilityShortDTO techCapabilityDTO = TechCapabilityShortDTO.convert(techCapability);
        List<BusinessCapability> businessCapabilities = techCapability.getParents().stream()
                .map(TechCapabilityRelations::getBusinessCapability)
                .filter(businessCapability -> Objects.isNull(businessCapability.getDeletedDate()))
                .collect(Collectors.toList());
        List<BusinessCapabilityDTO> parentDtos = BusinessCapabilityDTO.convert(businessCapabilities);

        CapabilityDTO techCapabilityDto = new CapabilityDTO();
        techCapabilityDto.setTech(techCapabilityDTO);
        techCapabilityDto.setOwner(techCapability.getOwner());

        techCapabilityDto.setParents(parentDtos);

        return techCapabilityDto;
    }
}
