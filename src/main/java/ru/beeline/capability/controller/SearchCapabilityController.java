/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.dto.SearchCapabilityDTO;
import ru.beeline.capability.service.SearchCapabilityService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1")
public class SearchCapabilityController {

    @Autowired
    private SearchCapabilityService searchCapabilityService;


    @GetMapping("/find")
    @ApiOperation(value = "Поиск по сущностям", response = SearchCapabilityDTO.class)
    public List<SearchCapabilityDTO> getAllTech(@RequestParam(value = "findBy", required = false, defaultValue = "ALL") String findBy,
                                                @RequestParam(value = "search") String search) {
        return searchCapabilityService.searchCapability(search, findBy);
    }
}
