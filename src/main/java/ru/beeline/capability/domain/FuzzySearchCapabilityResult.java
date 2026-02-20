/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuzzySearchCapabilityResult {
    private int id;
    private String vector;
    private String entityName;
}