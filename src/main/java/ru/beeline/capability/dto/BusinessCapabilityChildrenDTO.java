package ru.beeline.capability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
}
