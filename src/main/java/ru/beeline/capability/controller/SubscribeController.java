/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.EntityType.EntityType;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.CapabilitySubscribedDTO;
import ru.beeline.capability.dto.SearchCapabilityDTO;
import ru.beeline.capability.service.SubscribeService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/capabilities-subscribed")
public class SubscribeController {

    @Autowired
    private SubscribeService subscribeService;

    @ApiErrorCodes({400, 500})
    @GetMapping
    @Operation(summary = "Получение подписок на возможности",
            description = "Получение подписок на возможности",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<CapabilitySubscribedDTO> getCapabilitiesSubscribed(@RequestParam(value = "entity-type") EntityType entityType) {
        return subscribeService.getCapabilitiesSubscribed(entityType);
    }
}
