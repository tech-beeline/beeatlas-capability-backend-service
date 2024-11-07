package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public List<EnumCriteria> getCriteriaList() {
        return criteriaService.getCriteria();
    }
}
