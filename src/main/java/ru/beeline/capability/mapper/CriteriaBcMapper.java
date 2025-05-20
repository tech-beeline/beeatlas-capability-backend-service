package ru.beeline.capability.mapper;

import org.springframework.stereotype.Component;
import ru.beeline.capability.domain.CriteriasBc;
import ru.beeline.capability.dto.BusinessCapabilityCriteriaDTO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CriteriaBcMapper {

    public List<BusinessCapabilityCriteriaDTO> convert(List<CriteriasBc> businessCapabilityCriteria) {
        return businessCapabilityCriteria.stream().map(this::convert).collect(Collectors.toList());
    }

    public BusinessCapabilityCriteriaDTO convert(CriteriasBc criteria) {
        return BusinessCapabilityCriteriaDTO.builder()
                .criterionId(criteria.getCriterionId())
                .comment(criteria.getComment())
                .grade(criteria.getGrade())
                .value(criteria.getValue())
                .build();
    }
}