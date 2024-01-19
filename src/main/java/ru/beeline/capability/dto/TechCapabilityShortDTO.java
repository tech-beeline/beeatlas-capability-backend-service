package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.beeline.capability.domain.TechCapability;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechCapabilityShortDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String type;
    private String author;
    private String link;
    private Date createdDate;
    @JsonProperty("updatedDate")
    private Date lastModifiedDate;

    public static List<TechCapabilityShortDTO> convert(List<TechCapability> techCapabilities) {
        List<TechCapabilityShortDTO> techCapabilityDTOS = new ArrayList<>();
        for (TechCapability techCapability : techCapabilities) {
            TechCapabilityShortDTO techCapabilityDTO = convert(techCapability);
            techCapabilityDTOS.add(techCapabilityDTO);
        }
        techCapabilityDTOS.sort(Comparator.comparing(TechCapabilityShortDTO::getName));
        return techCapabilityDTOS;
    }

    public static TechCapabilityShortDTO convert(TechCapability techCapability) {
        return TechCapabilityShortDTO.builder()
                .id(techCapability.getId())
                .code(techCapability.getCode())
                .name(techCapability.getName())
                .description(techCapability.getDescription())
                .author(techCapability.getAuthor())
                .link(techCapability.getLink())
                .createdDate(techCapability.getCreatedDate())
                .lastModifiedDate(techCapability.getLastModifiedDate())
                .build();
    }
}
