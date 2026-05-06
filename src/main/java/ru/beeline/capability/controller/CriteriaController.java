/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.domain.EnumCriteria;
import ru.beeline.capability.service.CriteriaService;

import java.util.List;


@RestController
@RequestMapping("/api/v1/criterias")
public class CriteriaController {

    @Autowired
    private CriteriaService criteriaService;

    @ApiErrorCodes({400, 500})
    @GetMapping
    @Operation(summary = "Получение критериев",
            description = "Возвращает список критериев с учетом опционального фильтра.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EnumCriteria.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<EnumCriteria> getCriteriaList(@RequestParam(required = false) String filter) {
        return criteriaService.getCriteria(filter);
    }
}
