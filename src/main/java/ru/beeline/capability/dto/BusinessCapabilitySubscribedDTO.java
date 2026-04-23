/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessCapabilitySubscribedDTO extends CapabilitySubscribedDTO{

    @JsonProperty("parent_id")
    private Long parentId;

    public BusinessCapabilitySubscribedDTO(Long parentId,
                                           Long id,
                                           String code,
                                           String name,
                                           String description,
                                           Boolean isDomain,
                                           String owner){
        super(id, code, name, description, isDomain, owner);
        this.parentId = parentId;
    }
}
