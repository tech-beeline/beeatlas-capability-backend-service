package ru.beeline.capability.dto;

import lombok.*;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityChildrenDTO {
    private List<TechCapabilityShortDTO> techCapabilities;
    private List<BusinessCapabilityDTO> businessCapabilities;

    public static BusinessCapabilityChildrenDTO convert(List<TechCapability> children, List<BusinessCapability> businessCapabilities) {
        BusinessCapabilityChildrenDTO businessCapabilityChildrenDTO = new BusinessCapabilityChildrenDTO();
        businessCapabilityChildrenDTO.setTechCapabilities(TechCapabilityShortDTO.convert(children));
        businessCapabilityChildrenDTO.setBusinessCapabilities(BusinessCapabilityDTO.convert(businessCapabilities));
        return businessCapabilityChildrenDTO;
    }
}
