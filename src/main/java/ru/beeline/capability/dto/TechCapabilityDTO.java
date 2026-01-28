package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.fdmlib.dto.product.GetProductsByIdsDTO;

import java.util.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechCapabilityDTO {

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
    private String owner;
    private List<BCParentDTO> parents;
    @JsonIgnore
    private Integer systemId;
    private GetProductsByIdsDTO system;

    public static List<TechCapabilityDTO> convert(List<TechCapability> techCapabilities,
                                                  Map<Integer, GetProductsByIdsDTO> tcMap) {
        List<TechCapabilityDTO> techCapabilityDTOS = new ArrayList<>();
        for (TechCapability techCapability : techCapabilities) {
            TechCapabilityDTO techCapabilityDTO = convert(techCapability,
                    tcMap.get(techCapability.getResponsibilityProductId()));
            techCapabilityDTOS.add(techCapabilityDTO);
        }
        return techCapabilityDTOS;
    }

    public static TechCapabilityDTO convert(TechCapability techCapability, GetProductsByIdsDTO system) {
        if (Objects.isNull(techCapability)) {
            return null;
        }
        return TechCapabilityDTO.builder()
                .id(techCapability.getId())
                .code(techCapability.getCode())
                .name(techCapability.getName())
                .description(techCapability.getDescription())
                .author(techCapability.getAuthor())
                .link(techCapability.getLink())
                .createdDate(techCapability.getCreatedDate())
                .lastModifiedDate(techCapability.getLastModifiedDate())
                .deletedDate(techCapability.getDeletedDate())
                .owner(techCapability.getOwner())
                .parents(BCParentDTO.convert(techCapability.getParents()))
                .systemId(techCapability.getResponsibilityProductId())
                .system(system)
                .build();
    }
}
