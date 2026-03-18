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
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.CapabilityExportDTO;
import ru.beeline.capability.service.CapabilityExportService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/export")
public class CapabilityExportController {

    @Autowired
    CapabilityExportService capabilityExportService;

    @ApiErrorCodes({400, 500})
    @PostMapping("/business-capability/{doc_id}")
    @Operation(summary = "Экспорт бизнес-возможностей",
            description = "Экспорт бизнес-возможностей",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Создано",
                            content = @Content(schema = @Schema(implementation = CapabilityExportDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity<CapabilityExportDTO> postExportBusinessCapabilities(@PathVariable(name = "doc_id") Integer docId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(capabilityExportService.postExportBusinessCapabilities(docId));
    }

    @ApiErrorCodes({400, 500})
    @PostMapping("/tech-capability/{doc_id}")
    @Operation(summary = "Экспорт технических возможностей",
            description = "Экспорт технических возможностей",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Создано",
                            content = @Content(schema = @Schema(implementation = CapabilityExportDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity<CapabilityExportDTO> postExportTechCapabilities(@PathVariable(name = "doc_id") Integer docId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(capabilityExportService.postExportTechCapabilities(docId));
    }
}
