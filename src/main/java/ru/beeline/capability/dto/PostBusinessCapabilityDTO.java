/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostBusinessCapabilityDTO {
    private String code;
    private Boolean isDomain;
    private String name;
    private String description;
    private Date modifiedDate;
    private Date createdDate;
    private String author;
    private String link;
    private String owner;
    private String status;
    private String parent;
}