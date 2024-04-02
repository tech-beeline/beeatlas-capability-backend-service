package ru.beeline.capability.dto;

import lombok.*;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.mapper.BusinessCapabilityMapper;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityDTO;
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
        businessCapabilityChildrenDTO.setBusinessCapabilities(BusinessCapabilityMapper.convert(businessCapabilities));
        return businessCapabilityChildrenDTO;
    }
}
