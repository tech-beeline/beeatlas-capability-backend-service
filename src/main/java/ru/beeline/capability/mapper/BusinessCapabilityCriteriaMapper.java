package ru.beeline.capability.mapper;

import org.springframework.stereotype.Component;
import ru.beeline.capability.domain.BusinessCapabilityCriteria;
import ru.beeline.capability.dto.BusinessCapabilityCriteriaDTO;

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