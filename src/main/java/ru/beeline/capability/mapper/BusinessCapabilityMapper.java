package ru.beeline.capability.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.dto.BCParentDTO;
import ru.beeline.capability.dto.BusinessCapabilityShortDTO;
import ru.beeline.capability.dto.BusinessCapabilityTreeCustomDTO;
import ru.beeline.capability.dto.BusinessCapabilityTreeDTO;
import ru.beeline.capability.dto.BusinessCapabilityTreeInfoDTO;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.TechCapabilityRelationsRepository;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenDTO;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenDTOV2;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityDTO;
import ru.beeline.fdmlib.dto.capability.PutBusinessCapabilityDTO;
import ru.beeline.fdmlib.dto.product.GetProductsByIdsDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BusinessCapabilityMapper {

    @Autowired
    private CriteriaBcMapper criteriaBcMapper;

    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;

    @Autowired
    private TechCapabilityRelationsRepository techCapabilityRelationsRepository;

    public List<BusinessCapabilityDTO> convert(List<BusinessCapability> businessCapabilities) {
        List<BusinessCapabilityDTO> result = businessCapabilities.stream().map(this::convert).collect(Collectors.toList());
        result.sort(Comparator.comparing(BusinessCapabilityDTO::getName));
        return result;

    }

    public BusinessCapabilityDTO convert(BusinessCapability businessCapability) {
        return BusinessCapabilityDTO.builder()
                .id(businessCapability.getId())
                .code(businessCapability.getCode())
                .name(businessCapability.getName())
                .description(businessCapability.getDescription())
                .isDomain(businessCapability.isDomain())
                .author(businessCapability.getAuthor())
                .owner(businessCapability.getOwner())
                .link(businessCapability.getLink())
                .createdDate(businessCapability.getCreatedDate())
                .hasChildren(isAnyChildrenBcNotDeleted(businessCapability.getId())
                        || isAnyChildrenTcNotDeleted(businessCapability))
                .build();
    }

    private Boolean isAnyChildrenBcNotDeleted(Long parentId) {
        return businessCapabilityRepository.existsByParentIdAndDeletedDateIsNull(parentId);
    }

    private Boolean isAnyChildrenTcNotDeleted(BusinessCapability businessCapability) {
        return techCapabilityRelationsRepository.existsByBusinessCapabilityAndTechCapability_DeletedDateIsNull(businessCapability);
    }

    public PutBusinessCapabilityDTO convertToPutCapabilityDTO(BusinessCapability businessCapability) {
        return PutBusinessCapabilityDTO.builder()
                .code(businessCapability.getCode())
                .name(businessCapability.getName())
                .description(businessCapability.getDescription())
                .status(businessCapability.getStatus())
                .author(businessCapability.getAuthor())
                .link(businessCapability.getLink())
                .owner(businessCapability.getOwner())
                .isDomain(businessCapability.isDomain())
                .parent(getParentCode(businessCapability))
                .build();
    }

    private String getParentCode(BusinessCapability capability) {
        if (capability == null || capability.getParentId() == null)
            return null;
        return businessCapabilityRepository.findById(capability.getParentId())
                .map(BusinessCapability::getCode)
                .orElse(null);
    }

    public BusinessCapabilityChildrenDTO convert(List<TechCapability> children, List<BusinessCapability> businessCapabilities) {
        BusinessCapabilityChildrenDTO businessCapabilityChildrenDTO = new BusinessCapabilityChildrenDTO();
        businessCapabilityChildrenDTO.setTechCapabilities(TechCapabilityMapper.convertToTechCapabilityShortDTOList(children));
        businessCapabilityChildrenDTO.setBusinessCapabilities(convert(businessCapabilities));
        return businessCapabilityChildrenDTO;
    }

    public BusinessCapabilityChildrenDTOV2 convert(List<TechCapability> children,
                                                   List<GetProductsByIdsDTO> products,
                                                   List<BusinessCapability> businessCapabilities) {
        BusinessCapabilityChildrenDTOV2 businessCapabilityChildrenDTO = new BusinessCapabilityChildrenDTOV2();
        businessCapabilityChildrenDTO.setTechCapabilities(TechCapabilityMapper.convertToTechCapabilityShortDTOList(children, products));
        businessCapabilityChildrenDTO.setBusinessCapabilities(convert(businessCapabilities));
        return businessCapabilityChildrenDTO;
    }

    public List<BusinessCapabilityShortDTO> convertToBusinessCapabilityShortDTOList(
            List<BusinessCapability> businessCapabilities, String findBy) {
        List<Long> parentIds;
        if ("CORE".equals(findBy)) {
            parentIds = businessCapabilities.stream()
                    .filter(bc -> bc.getParentId() == null)
                    .map(BusinessCapability::getId)
                    .collect(Collectors.toList());
        } else {
            parentIds = businessCapabilities.stream()
                    .map(BusinessCapability::getId)
                    .collect(Collectors.toList());
        }
        List<Long> activeBcIds = findActiveBusinessCapabilityIds(parentIds);
        List<Long> activeTcIds = findActiveTechCapabilities(parentIds);
        return businessCapabilities.stream()
                .map(bc -> {
                    boolean hasActiveChildren = activeBcIds.contains(bc.getId()) || activeTcIds.contains(bc.getId());
                    return convert(bc, hasActiveChildren);
                })
                .collect(Collectors.toList());
    }

    private List<Long> findActiveBusinessCapabilityIds(List<Long> parentIds) {
        if (parentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return businessCapabilityRepository.findActiveBusinessCapabilities(parentIds);
    }

    private List<Long> findActiveTechCapabilities(List<Long> businessCapabilityIds) {
        if (businessCapabilityIds.isEmpty()) {
            return Collections.emptyList();
        }
        return techCapabilityRelationsRepository.findActiveTechCapabilities(businessCapabilityIds);
    }

    public BusinessCapabilityShortDTO convert(BusinessCapability businessCapability, boolean hasKids) {
        return BusinessCapabilityShortDTO.builder()
                .id(businessCapability.getId())
                .code(businessCapability.getCode())
                .name(businessCapability.getName())
                .description(businessCapability.getDescription())
                .author(businessCapability.getAuthor())
                .link(businessCapability.getLink())
                .createdDate(businessCapability.getCreatedDate())
                .lastModifiedDate(businessCapability.getLastModifiedDate())
                .deletedDate(businessCapability.getDeletedDate())
                .owner(businessCapability.getOwner())
                .isDomain(businessCapability.isDomain())
                .hasChildren(hasKids)
                .parent(BCParentDTO.convert(businessCapability.getParentEntity()))
                .build();
    }

    public List<BusinessCapabilityTreeDTO> mapToTree(List<BusinessCapability> businessCapabilities) {
        return businessCapabilities.stream()
                .filter(bc -> bc.getDeletedDate() == null)
                .map(businessCapability -> {
                    businessCapability.setChildrenOfTree(getChildrenBC(businessCapability).stream()
                            .filter(bc -> bc.isDomain() == businessCapability.isDomain())
                            .collect(Collectors.toList()));
                    return BusinessCapabilityTreeDTO.builder()
                            .id(businessCapability.getId())
                            .code(businessCapability.getCode())
                            .name(businessCapability.getName())
                            .description(businessCapability.getDescription())
                            .author(businessCapability.getAuthor())
                            .status(businessCapability.getStatus())
                            .link(businessCapability.getLink())
                            .createdDate(businessCapability.getCreatedDate())
                            .lastModifiedDate(businessCapability.getLastModifiedDate())
                            .isDomain(businessCapability.isDomain())
                            .owner(businessCapability.getOwner())
                            .parentId(businessCapability.getParentId())
                            .criteria(criteriaBcMapper.convert(businessCapability.getCriteria()))
                            .children(mapToTree(businessCapability.getChildrenOfTree()))
                            .build();
                }).collect(Collectors.toList());
    }

    private List<BusinessCapability> getChildrenBC(BusinessCapability businessCapability) {
        return businessCapabilityRepository.findAllByParentId(businessCapability.getId());
    }

    public BusinessCapabilityTreeInfoDTO mapToTreeInfo(BusinessCapability businessCapability) {
        return BusinessCapabilityTreeInfoDTO.builder()
                .id(businessCapability.getId())
                .code(businessCapability.getCode())
                .name(businessCapability.getName())
                .description(businessCapability.getDescription())
                .author(businessCapability.getAuthor())
                .status(businessCapability.getStatus())
                .link(businessCapability.getLink())
                .createdDate(businessCapability.getCreatedDate())
                .lastModifiedDate(businessCapability.getLastModifiedDate())
                .isDomain(businessCapability.isDomain())
                .owner(businessCapability.getOwner())
                .parentId(businessCapability.getParentId())
                .criteria(criteriaBcMapper.convert(businessCapability.getCriteria()))
                .build();
    }

    public BusinessCapabilityTreeCustomDTO mapToCustomTree(List<BusinessCapability> businessCapabilities, BusinessCapability businessCapability) {
        return BusinessCapabilityTreeCustomDTO.builder()
                .id(businessCapability.getId())
                .code(businessCapability.getCode())
                .name(businessCapability.getName())
                .description(businessCapability.getDescription())
                .author(businessCapability.getAuthor())
                .status(businessCapability.getStatus())
                .link(businessCapability.getLink())
                .createdDate(businessCapability.getCreatedDate())
                .lastModifiedDate(businessCapability.getLastModifiedDate())
                .isDomain(businessCapability.isDomain())
                .parentId(businessCapability.getParentId())
                .owner(businessCapability.getOwner())
                .criteria(criteriaBcMapper.convert(businessCapability.getCriteria()))
                .children(mapToTree(businessCapabilities))
                .parent(getParentList(businessCapability.getParentEntity(), new ArrayList<BusinessCapabilityTreeInfoDTO>()))
                .build();
    }

    private List<BusinessCapabilityTreeInfoDTO> getParentList(BusinessCapability businessCapability, ArrayList<BusinessCapabilityTreeInfoDTO> businessCapabilityTreeInfoDTOS) {
        if (businessCapability != null && businessCapability.getDeletedDate() == null) {
            businessCapabilityTreeInfoDTOS.add(mapToTreeInfo(businessCapability));
            return getParentList(businessCapability.getParentEntity(), businessCapabilityTreeInfoDTOS);
        }
        return businessCapabilityTreeInfoDTOS;
    }
}