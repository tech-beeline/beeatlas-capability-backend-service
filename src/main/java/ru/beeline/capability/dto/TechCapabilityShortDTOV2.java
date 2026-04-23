/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.beeline.capability.dto.product.GetProductsByIdsDTO;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechCapabilityShortDTOV2 {

    private String author;
    private String code;
    private Date createdDate;
    private String description;
    private Long id;
    private String link;
    private String name;
    private String owner;
    private GetProductsByIdsDTO product;
    private String type;
    @JsonProperty("updatedDate")
    private Date lastModifiedDate;
}
