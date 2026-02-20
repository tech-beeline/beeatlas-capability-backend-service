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
import ru.beeline.capability.EntityType.EntityType;
import ru.beeline.capability.dto.CapabilitySubscribedDTO;
import ru.beeline.capability.service.SubscribeService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/capabilities-subscribed")
public class SubscribeController {

    @Autowired
    private SubscribeService subscribeService;

    @GetMapping
    @ApiOperation(value = "Получение подписок на возможности")
    public List<CapabilitySubscribedDTO> getCapabilitiesSubscribed(@RequestParam(value = "entity-type") EntityType entityType) {
        return subscribeService.getCapabilitiesSubscribed(entityType);
    }

}
