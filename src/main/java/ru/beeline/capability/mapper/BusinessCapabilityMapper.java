package ru.beeline.capability.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.dto.BusinessCapabilityChildrenDTO;
import ru.beeline.capability.dto.CapabilitySubscribedDTO;
import ru.beeline.capability.dto.TechCapabilityShortDTO;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityDTO;
import ru.beeline.fdmlib.dto.capability.PutBusinessCapabilityDTO;
import ru.beeline.capability.repository.BusinessCapabilityRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BusinessCapabilityMapper {

    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;

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
                .hasChildren(!businessCapability.getChildren().isEmpty())
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
                .parent(businessCapabilityRepository.findById(businessCapability.getParentId()).get().getCode())
                .build();
    }

    public BusinessCapabilityChildrenDTO convert(List<TechCapability> children, List<BusinessCapability> businessCapabilities) {
        BusinessCapabilityChildrenDTO businessCapabilityChildrenDTO = new BusinessCapabilityChildrenDTO();
        businessCapabilityChildrenDTO.setTechCapabilities(TechCapabilityShortDTO.convert(children));
        businessCapabilityChildrenDTO.setBusinessCapabilities(convert(businessCapabilities));
        return businessCapabilityChildrenDTO;
    }

    public List<CapabilitySubscribedDTO> convertToCapabilitySubscribedDTOs(List<BusinessCapability> businessCapabilities) {
        return businessCapabilities.stream().map(this::convertToCapabilitySubscribedDTO).collect(Collectors.toList());
    }

    public CapabilitySubscribedDTO convertToCapabilitySubscribedDTO(BusinessCapability businessCapabilities) {
        return CapabilitySubscribedDTO.builder()
                .id(businessCapabilities.getId())
                .code(businessCapabilities.getCode())
                .name(businessCapabilities.getName())
                .description(businessCapabilities.getDescription())
                .isDomain(businessCapabilities.isDomain())
                .owner(businessCapabilities.getOwner())
                .build();
    }
}