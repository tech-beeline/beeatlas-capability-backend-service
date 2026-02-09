/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostPromtDTO {

    private String promtAlias;
    private String promtTarget;
}
