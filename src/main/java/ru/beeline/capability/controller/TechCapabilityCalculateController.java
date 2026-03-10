/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.service.TechCapabilityService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1")
public class TechCapabilityCalculateController {

    @Autowired
    private TechCapabilityService techCapabilityService;

    @ApiErrorCodes({400, 500})
    @PostMapping("/calculate-total-tech-capabilities")
    @Operation(summary = "Запустить процесс общего расчета критериев для тепловых карт",
            description = "Запустить процесс общего расчета критериев для тепловых карт",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity calculateTotalTechCapabilitiesCount() {
        techCapabilityService.calculateTotalTechCapabilitiesCount();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/tech-capability/recount-quality")
    @Operation(summary = "Вызова процесса пересчета качества описания ТС",
            description = "Вызова процесса пересчета качества описания ТС",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity getTechRecalculationProcess() {
        techCapabilityService.getTechRecalculationProcess();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
