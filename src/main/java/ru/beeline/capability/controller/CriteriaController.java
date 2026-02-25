/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.domain.EnumCriteria;
import ru.beeline.capability.service.CriteriaService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/criterias")
public class CriteriaController {

    @Autowired
    private CriteriaService criteriaService;

    @GetMapping
    @ApiOperation(value = "Получение критерий")
    public List<EnumCriteria> getCriteriaList(@RequestParam(required = false) String filter) {
        return criteriaService.getCriteria(filter);
    }
}
