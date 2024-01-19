package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.beeline.capability.domain.BusinessCapability;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String author;
    private String status;
    private String link;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date createdDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date updatedDate;
    private boolean isDomain;
    private boolean hasChildren;

    public static List<BusinessCapabilityDTO> convert(List<BusinessCapability> businessCapabilities) {
        List<BusinessCapabilityDTO> businessCapabilityDTOS = new ArrayList<>();
        for (BusinessCapability businessCapability : businessCapabilities) {
            BusinessCapabilityDTO businessCapabilityDTO = convert(businessCapability);
            businessCapabilityDTOS.add(businessCapabilityDTO);
        }
        businessCapabilityDTOS.sort(Comparator.comparing(BusinessCapabilityDTO::getName));
        return businessCapabilityDTOS;
    }

    public static BusinessCapabilityDTO convert(BusinessCapability techCapability) {
        return BusinessCapabilityDTO.builder()
                .id(techCapability.getId())
                .code(techCapability.getCode())
                .name(techCapability.getName())
                .description(techCapability.getDescription())
                .author(techCapability.getAuthor())
                .link(techCapability.getLink())
                .createdDate(techCapability.getCreatedDate())
                .hasChildren(!techCapability.getChildren().isEmpty())
                .build();
    }
}
