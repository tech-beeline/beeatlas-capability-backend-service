package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.beeline.capability.domain.*;
import ru.beeline.capability.dto.CapabilityParentDTO;
import org.springframework.transaction.annotation.Transactional;
import ru.beeline.capability.dto.TechCapabilityDTO;
import ru.beeline.capability.dto.UpdateTechCapabilityDTO;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.repository.*;

import java.util.*;
import java.util.stream.Collectors;

import static ru.beeline.capability.utils.Constants.ENTITY_TYPE_TECH_CAPABILITY;

@Service
public class TechCapabilityService {

    @Autowired
    private BusinessCapabilityService businessCapabilityService;

    @Autowired
    private TechCapabilityRepository techCapabilityRepository;

    @Autowired
    private TechCapabilityRelationsRepository techCapabilityRelationsRepository;
    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;

    @Autowired
    private FindNameSortTableRepository findNameSortTableRepository;

    @Autowired
    private EntityTypeRepository entityTypeRepository;

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

    @Transactional
    public void createOrUpdate(UpdateTechCapabilityDTO techCapability) {
        if(techCapability.getParents() != null && !techCapability.getParents().isEmpty()) {
            List<BusinessCapability> businessCapabilities = businessCapabilityRepository.findAllByCodeIn(techCapability.getParents());
            TechCapability currentTechCapability = techCapabilityRepository.findByCode(techCapability.getCode());
            if(currentTechCapability == null) {
                currentTechCapability = createTechCapability(techCapability);
            } else {
                updateTechCapability(currentTechCapability, techCapability);
                deleteAllRelationsByTCAndBC(currentTechCapability, businessCapabilities);
            }
            if(businessCapabilities != null && !businessCapabilities.isEmpty())
                createRelations(currentTechCapability, businessCapabilities);
            updateVector(currentTechCapability);
        }
    }

    private void updateVector(TechCapability techCapability) {
        String vector = String.join("<!!!>", new String[] {
                                                                    techCapability.getName(),
                                                                    techCapability.getDescription(),
                                                                    techCapability.getCode()
        });
        EntityType entityType = entityTypeRepository.findByName(ENTITY_TYPE_TECH_CAPABILITY);
        FindNameSortTable findNameSortTableItem = findNameSortTableRepository.findByRefIdAndType(techCapability.getId(), entityType);
        if(findNameSortTableItem == null) {
            findNameSortTableItem = FindNameSortTable.builder()
                    .vector(vector)
                    .type(entityType)
                    .refId(techCapability.getId())
                    .build();
        } else {
            findNameSortTableItem.setVector(vector);
        }
        findNameSortTableRepository.save(findNameSortTableItem);
    }

    private void deleteAllRelationsByTCAndBC(TechCapability currentTechCapability, List<BusinessCapability> businessCapabilities) {
        for (BusinessCapability businessCapability : businessCapabilities) {
            techCapabilityRelationsRepository.deleteByBusinessCapabilityAndTechCapability(businessCapability, currentTechCapability);
        }
    }

    private void createRelations(TechCapability currentTechCapability, List<BusinessCapability> businessCapabilities) {
        List<TechCapabilityRelations> techCapabilityRelations = new ArrayList<>();
        for (BusinessCapability businessCapability : businessCapabilities) {
            TechCapabilityRelations techCapabilityRelation = new TechCapabilityRelations();
            techCapabilityRelation.setBusinessCapability(businessCapability);
            techCapabilityRelation.setTechCapability(currentTechCapability);
            techCapabilityRelations.add(techCapabilityRelation);
        }
        techCapabilityRelationsRepository.saveAll(techCapabilityRelations);
    }

    private void updateTechCapability(TechCapability currentTechCapability, UpdateTechCapabilityDTO techCapability) {
        currentTechCapability.setName(techCapability.getName());
        currentTechCapability.setDescription(techCapability.getDescription());
        currentTechCapability.setAuthor(techCapability.getAuthor());
        currentTechCapability.setOwner(techCapability.getOwner());
        currentTechCapability.setLink(techCapability.getLink());
        currentTechCapability.setStatus(techCapability.getStatus());
        techCapabilityRepository.save(currentTechCapability);
    }

    private TechCapability createTechCapability(UpdateTechCapabilityDTO techCapability) {
        TechCapability newTechCapability = TechCapability.builder()
                .code(techCapability.getCode())
                .name(techCapability.getName())
                .description(techCapability.getDescription())
                .author(techCapability.getAuthor())
                .owner(techCapability.getOwner())
                .link(techCapability.getLink())
                .status(techCapability.getStatus())
                .build();
        techCapabilityRepository.save(newTechCapability);
        return newTechCapability;
    }
}
