package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityShortDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String author;
    private String link;
    private Date createdDate;
    @JsonProperty("updatedDate")
    private Date lastModifiedDate;
    private Date deletedDate;
    private Long owner;
    private boolean isDomain;
    private boolean hasChildren;

    private BCParentDTO parent;

    public static List<BusinessCapabilityShortDTO> convert(List<BusinessCapability> businessCapabilities) {
        List<BusinessCapabilityShortDTO> techCapabilityDTOS = new ArrayList<>();
        for (BusinessCapability businessCapability : businessCapabilities) {
            BusinessCapabilityShortDTO techCapabilityDTO = convert(businessCapability, true);
            techCapabilityDTOS.add(techCapabilityDTO);
        }
        return techCapabilityDTOS;
    }

    public static BusinessCapabilityShortDTO convert(BusinessCapability businessCapability, boolean hasCKids) {
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
                .hasChildren(hasCKids)
                .parent(BCParentDTO.convert(businessCapability.getParentEntity()))
                .build();
    }
}
