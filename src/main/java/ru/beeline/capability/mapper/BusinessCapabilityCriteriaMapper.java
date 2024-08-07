package ru.beeline.capability.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.BusinessCapabilityCriteria;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.dto.BCParentDTO;
import ru.beeline.capability.dto.BusinessCapabilityCriteriaDTO;
import ru.beeline.capability.dto.BusinessCapabilityShortDTO;
import ru.beeline.capability.dto.BusinessCapabilityTreeDTO;
import ru.beeline.capability.dto.CapabilitySubscribedDTO;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.TechCapabilityRelationsRepository;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenDTO;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityDTO;
import ru.beeline.fdmlib.dto.capability.PutBusinessCapabilityDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BusinessCapabilityCriteriaMapper {

    public List<BusinessCapabilityCriteriaDTO> convert(List<BusinessCapabilityCriteria> businessCapabilityCriteria) {
        return businessCapabilityCriteria.stream().map(this::convert).collect(Collectors.toList());
    }

    public BusinessCapabilityCriteriaDTO convert(BusinessCapabilityCriteria criteria) {
        return BusinessCapabilityCriteriaDTO.builder()
                .criterionId(criteria.getCriterionId())
                .grade(criteria.getGrade())
                .value(criteria.getValue())
                .build();
    }
}