package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.beeline.capability.domain.BusinessCapability;

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
    private Long owner;
    private boolean isDomain;
    private boolean hasChildren;
    private BCParentDTO parent;

    public static BusinessCapabilityShortDTO convert(BusinessCapability businessCapability, BusinessCapability parent, boolean hasCKids) {
        return BusinessCapabilityShortDTO.builder()
                .id(businessCapability.getId())
                .code(businessCapability.getCode())
                .name(businessCapability.getName())
                .description(businessCapability.getDescription())
                .author(businessCapability.getAuthor())
                .link(businessCapability.getLink())
                .createdDate(businessCapability.getCreatedDate())
                .lastModifiedDate(businessCapability.getLastModifiedDate())
                .owner(businessCapability.getOwner())
                .isDomain(businessCapability.isDomain())
                .hasChildren(hasCKids)
                .parent(BCParentDTO.convert(parent))
                .build();
    }
}
