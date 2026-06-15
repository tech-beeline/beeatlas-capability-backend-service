/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller.v2;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.BusinessCapabilityChildrenDTOV2;
import ru.beeline.capability.service.BusinessCapabilityService;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v2/business-capability")
@Tag(name = "Бизнес-возможности v2", description = "CRUD и операции с бизнес-возможностями (BC) меньше информации по TC")
public class BusinessCapabilityControllerV2 {

    @Autowired
    private BusinessCapabilityService businessCapabilityService;

    @ApiErrorCodes({400, 500})
    @GetMapping("/{id}/children")
    @Operation(summary = "Получение дочерних бизнес-возможностей (v2)",
            description = "Возвращает DTO с дочерними возможностями",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = BusinessCapabilityChildrenDTOV2.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public BusinessCapabilityChildrenDTOV2 getKidsById(@PathVariable Long id) {
        return businessCapabilityService.getChildrenV2(id);
    }
}
