package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;
import ru.beeline.capability.dto.CapabilityParentDTO;
import ru.beeline.capability.dto.TechCapabilityDTO;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.repository.TechCapabilityRelationsRepository;
import ru.beeline.capability.repository.TechCapabilityRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TechCapabilityService {

    @Autowired
    private BusinessCapabilityService businessCapabilityService;

    @Autowired
    private TechCapabilityRepository techCapabilityRepository;

    @Autowired
    private TechCapabilityRelationsRepository techCapabilityRelationsRepository;

    public List<TechCapabilityDTO> getCapabilities(Integer limit, Integer offset) {
        if (offset == null) {
            offset = 0;
        }
        Pageable pageable = new OffsetBasedPageRequest(offset, limit == null || limit == 0 ? Integer.MAX_VALUE : limit, Sort.by(Sort.Direction.ASC, "name"));
        Page<TechCapability> techCapabilities = techCapabilityRepository.findCapabilities(pageable);
        return TechCapabilityDTO.convert(techCapabilities.toList());
    }

    public TechCapabilityDTO getCapabilityById(Long id) {
        TechCapability techCapability = techCapabilityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tech Capability не найдено"));
        techCapability.setParents(techCapability.getParents().stream()
                .filter(businessCapability -> Objects.isNull(businessCapability.getBusinessCapability().getDeletedDate()))
                .collect(Collectors.toList()));
        return TechCapabilityDTO.convert(techCapability);
    }

    public CapabilityParentDTO getParents(Long id) {
        ArrayList<CapabilityParentDTO> parents = new ArrayList<>();
        TechCapability techCapability = techCapabilityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tech Capability не найдено"));
        List<TechCapabilityRelations> techCapabilityRelations = techCapabilityRelationsRepository.findByTechCapability(techCapability);
        if (!techCapabilityRelations.isEmpty()) {
            parents.add(
                    new CapabilityParentDTO(
                            techCapabilityRelations.stream()
                            .map(TechCapabilityRelations::getBusinessCapability)
                            .map(BusinessCapability::getId)
                            .collect(Collectors.toList())
                    )
            );
            techCapabilityRelations.forEach(relation -> {
                parents.add(businessCapabilityService.getParents(relation.getBusinessCapability().getId()));
            });
        }

        return mergeAndRemoveDuplicates(parents);
    }

    private CapabilityParentDTO mergeAndRemoveDuplicates(ArrayList<CapabilityParentDTO> parentsList) {
        Set<Long> uniqueParents = new HashSet<>();

        for (CapabilityParentDTO dto : parentsList) {
            uniqueParents.addAll(dto.getParents());
        }

        CapabilityParentDTO result = new CapabilityParentDTO();
        result.setParents(new ArrayList<>(uniqueParents));

        return result;
    }
}
