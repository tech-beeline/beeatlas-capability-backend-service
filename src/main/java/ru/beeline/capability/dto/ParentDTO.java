/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentDTO {

    private Long id;
    private String code;
    private String name;
    private Date createdDate;
    private Date deletedDate;
}
