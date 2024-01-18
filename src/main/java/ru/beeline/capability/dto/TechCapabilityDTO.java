package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private Long owner;
    private List<BCParentDTO> parents;

    public static List<TechCapabilityDTO> convert(List<TechCapability> techCapabilities, Map<TechCapability, List<BusinessCapability>> techCapabilitiesWithParentsMap) {
        List<TechCapabilityDTO> techCapabilityDTOS = new ArrayList<>();
        for(TechCapability techCapability : techCapabilities) {
            TechCapabilityDTO techCapabilityDTO = TechCapabilityDTO.builder()
                    .id(techCapability.getId())
                    .code(techCapability.getCode())
                    .name(techCapability.getName())
                    .description(techCapability.getDescription())
                    .author(techCapability.getAuthor())
                    .link(techCapability.getLink())
                    .createdDate(techCapability.getCreatedDate())
                    .lastModifiedDate(techCapability.getLastModifiedDate())
                    .owner(techCapability.getOwner())
                    .parents(BCParentDTO.convert(techCapabilitiesWithParentsMap.get(techCapability)))
                    .build();
            techCapabilityDTOS.add(techCapabilityDTO);
        }
        return techCapabilityDTOS;
    }
}
