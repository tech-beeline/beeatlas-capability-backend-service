/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto;

import com.sun.istack.NotNull;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityOrderPatchRequestDTO {

    @NotNull
    private String name;
    @NotNull
    private String description;
    @NotNull
    private String owner;
    @NotNull
    private Integer parentId;
    private String comment;

}
