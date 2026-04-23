/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityTreeCustomDTO {

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
    @JsonProperty("isDomain")
    private boolean isDomain;
    private Long parentId;
    private String owner;
    private List<BusinessCapabilityCriteriaDTO>  criteria;
    private List<BusinessCapabilityTreeDTO> children;
    private List<BusinessCapabilityTreeInfoDTO> parent;
}
