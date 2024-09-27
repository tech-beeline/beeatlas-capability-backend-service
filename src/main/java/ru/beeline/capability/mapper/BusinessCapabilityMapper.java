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
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityDTO;
import ru.beeline.fdmlib.dto.capability.PutBusinessCapabilityDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BusinessCapabilityMapper {

    @Autowired
    private BusinessCapabilityCriteriaMapper businessCapabilityCriteriaMapper;

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
                .link(businessCapability.getLink())
                .createdDate(businessCapability.getCreatedDate())
                .hasChildren(businessCapabilityRepository.existsByParentId(businessCapability.getId())
                        || techCapabilityRelationsRepository.existsByBusinessCapability(businessCapability))
                .build();
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

    public List<BusinessCapabilityShortDTO> convertToBusinessCapabilityShortDTOList(List<BusinessCapability> businessCapabilities) {
        List<BusinessCapabilityShortDTO> techCapabilityDTOS = new ArrayList<>();
        for (BusinessCapability businessCapability : businessCapabilities) {
            BusinessCapabilityShortDTO techCapabilityDTO = convert(businessCapability,
                    businessCapabilityRepository.existsByParentId(businessCapability.getId())
                            || techCapabilityRelationsRepository.existsByBusinessCapability(businessCapability));
            techCapabilityDTOS.add(techCapabilityDTO);
        }
        return techCapabilityDTOS;
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
        return businessCapabilities.stream().map(businessCapability -> {
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
                    .criteria(businessCapabilityCriteriaMapper.convert(businessCapability.getCriteria()))
                    .children(mapToTree(businessCapability.getChildrenOfTree()))
                    .build();
        }).collect(Collectors.toList());
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
                .criteria(businessCapabilityCriteriaMapper.convert(businessCapability.getCriteria()))
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
                .owner(businessCapability.getOwner())
                .criteria(businessCapabilityCriteriaMapper.convert(businessCapability.getCriteria()))
                .children(mapToTree(businessCapabilities))
                .parent(getParentList(businessCapability.getParentEntity(), new ArrayList<BusinessCapabilityTreeInfoDTO>()))
                .build();
    }

    private List<BusinessCapabilityTreeInfoDTO> getParentList(BusinessCapability businessCapability, ArrayList<BusinessCapabilityTreeInfoDTO> businessCapabilityTreeInfoDTOS) {
        if (businessCapability != null) {
            businessCapabilityTreeInfoDTOS.add(mapToTreeInfo(businessCapability));
            if (businessCapability.getParentEntity() != null) {
                return getParentList(businessCapability.getParentEntity(), businessCapabilityTreeInfoDTOS);
            }
        }
        return businessCapabilityTreeInfoDTOS;
    }
}