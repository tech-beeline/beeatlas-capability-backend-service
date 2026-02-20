/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.domain.EntityType;
import ru.beeline.capability.service.CapabilityMapService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static ru.beeline.capability.utils.Constants.USER_ID_HEADER;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/capability")
public class CapabilityMapTypesController {

    @Autowired
    private CapabilityMapService capabilityMapService;

    @GetMapping("/type")
    @ApiOperation(value = "Получение всех типов карт")
    public List<EntityType> getCapabilityMapTypes(HttpServletRequest request) {
        return capabilityMapService.getCapabilityMapTypes(request.getHeader(USER_ID_HEADER));

    }
}
