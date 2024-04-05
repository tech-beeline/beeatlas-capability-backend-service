package ru.beeline.capability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class BusinessCapabilityParentDTO {
    private List<Long> parents;
}
