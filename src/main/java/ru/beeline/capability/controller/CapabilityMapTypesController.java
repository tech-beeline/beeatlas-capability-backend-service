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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.domain.EntityType;
import ru.beeline.capability.service.CapabilityMapService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static ru.beeline.capability.utils.Constants.USER_ID_HEADER;


@RestController
@RequestMapping("/api/v1/capability")
@Tag(name = "Типы карт", description = "Типы сущностей для карт возможностей")
public class CapabilityMapTypesController {

    @Autowired
    private CapabilityMapService capabilityMapService;

    @ApiErrorCodes({400, 500})
    @GetMapping("/type")
    @Operation(summary = "Получение всех типов карт",
            description = "Возвращает список типов карт, доступных пользователю.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EntityType.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<EntityType> getCapabilityMapTypes(HttpServletRequest request) {
        return capabilityMapService.getCapabilityMapTypes(request.getHeader(USER_ID_HEADER));

    }
}
