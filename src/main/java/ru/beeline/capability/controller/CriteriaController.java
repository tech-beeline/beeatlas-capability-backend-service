/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.domain.EnumCriteria;
import ru.beeline.capability.service.CriteriaService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/criterias")
public class CriteriaController {

    @Autowired
    private CriteriaService criteriaService;

    @ApiErrorCodes({400, 500})
    @GetMapping
    @Operation(summary = "Получение критерий",
            description = "Получение критерий",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<EnumCriteria> getCriteriaList(@RequestParam(required = false) String filter) {
        return criteriaService.getCriteria(filter);
    }
}
