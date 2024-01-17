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

    public static List<BCParentDTO> convert(List<BusinessCapability> businessCapabilities) {
        List<BCParentDTO> parents = new ArrayList<>();
        for (BusinessCapability businessCapability : businessCapabilities) {
            BCParentDTO bcParentDTO = BCParentDTO.builder()
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
            parents.add(bcParentDTO);
        }
        return parents;
    }
}
