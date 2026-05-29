/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.EntityType.EntityType;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.CapabilitySubscribedDTO;
import ru.beeline.capability.service.SubscribeService;

import java.util.List;

import static ru.beeline.capability.utils.Constants.USER_ID_HEADER;


@RestController
@RequestMapping("/api/v1/capabilities-subscribed")
@Tag(name = "Подписки на возможности", description = "Подписанные BC/TC по типу сущности")
public class SubscribeController {

    @Autowired
    private SubscribeService subscribeService;

    @ApiErrorCodes({400, 500})
    @GetMapping
    @Operation(summary = "Получение подписок на возможности",
            description = "Возвращает подписки пользователя на возможности по типу сущности.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CapabilitySubscribedDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<CapabilitySubscribedDTO> getCapabilitiesSubscribed(@RequestParam(value = "entity-type") EntityType entityType,
                                                                    @RequestHeader(value = USER_ID_HEADER) String userId) {
        return subscribeService.getCapabilitiesSubscribed(entityType, userId);
    }
}
