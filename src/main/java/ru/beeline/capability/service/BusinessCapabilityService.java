package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;
import ru.beeline.capability.dto.BusinessCapabilityChildrenDTO;
import ru.beeline.capability.repository.BusinessCapabilityRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BusinessCapabilityService {

    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;

    public BusinessCapabilityChildrenDTO getChildren(Long id) {
        List<TechCapability> techCapabilities = businessCapabilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business Capability не найдено"))
                .getChildren().stream()
                .map(TechCapabilityRelations::getTechCapability)
                .filter(techCapability -> Objects.isNull(techCapability.getDeletedDate()))
                .collect(Collectors.toList());
        List<BusinessCapability> businessCapabilitiesKids = businessCapabilityRepository.findAllByParentIdAndDeletedDateIsNull(id);
        return BusinessCapabilityChildrenDTO.convert(techCapabilities, businessCapabilitiesKids);
    }
}
