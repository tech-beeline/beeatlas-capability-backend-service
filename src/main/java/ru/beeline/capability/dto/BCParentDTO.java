package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BCParentDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String author;
    private String status;
    private String link;
    private Date createdDate;
    @JsonProperty("updatedDate")
    private Date lastModifiedDate;
    private boolean isDomain;
    private boolean hasChildren;

    public static List<BCParentDTO> convert(List<TechCapabilityRelations> relations) {
        List<BCParentDTO> parents = new ArrayList<>();
        for (TechCapabilityRelations relation : relations) {
            BCParentDTO bcParentDTO = convert(relation);
            parents.add(bcParentDTO);
        }
        return parents.stream()
                .sorted(Comparator.comparing(BCParentDTO::getName))
                .collect(Collectors.toList());
    }

    public static BCParentDTO convert(TechCapabilityRelations relation) {
        return BCParentDTO.builder()
                .id(relation.getBusinessCapability().getId())
                .code(relation.getBusinessCapability().getCode())
                .name(relation.getBusinessCapability().getName())
                .description(relation.getBusinessCapability().getDescription())
                .author(relation.getBusinessCapability().getAuthor())
                .status(relation.getBusinessCapability().getStatus())
                .link(relation.getBusinessCapability().getLink())
                .createdDate(relation.getBusinessCapability().getCreatedDate())
                .lastModifiedDate(relation.getBusinessCapability().getLastModifiedDate())
                .isDomain(relation.getBusinessCapability().isDomain())
                .hasChildren(true)
                .build();
    }

    public static BCParentDTO convert(BusinessCapability businessCapability) {
        return BCParentDTO.builder()
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
                .hasChildren(true)
                .build();
    }
}
