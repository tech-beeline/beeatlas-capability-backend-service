/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.*;
import ru.beeline.capability.service.TechCapabilityService;
import ru.beeline.capability.dto.PutTechCapabilityDTO;

import java.util.List;

import static ru.beeline.capability.utils.Constants.SOURCE;

@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/tech-capabilities")
public class TechCapabilityController {

    @Autowired
    private TechCapabilityService techCapabilityService;

    @ApiErrorCodes({400, 500})
    @GetMapping
    @Operation(summary = "Получение технических возможностей",
            description = "Получение технических возможностей",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<TechCapabilityDTO> getTechCapabilities(@RequestParam(value = "limit", required = false) Integer limit,
                                                       @RequestParam(value = "offset", required = false) Integer offset) {
        return techCapabilityService.getCapabilities(limit, offset);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/{id}")
    @Operation(summary = "Получение технической возможности",
            description = "Получение технической возможности",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = TechCapabilityDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public TechCapabilityDTO getAllTech(@PathVariable Long id) {
        return techCapabilityService.getCapabilityById(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/{id}/parents")
    @Operation(summary = "Получение всех родительских технических возможностей",
            description = "Получение всех родительских технических возможностей",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = CapabilityParentDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public CapabilityParentDTO getParentsById(@PathVariable Long id) {
        return techCapabilityService.getParents(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/history/{id}")
    @Operation(summary = "Получение списка версий TC",
            description = "Получение списка версий TC",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<GetHistoryByIdDTO> getTechCapabilityHistory(@PathVariable Long id) {
        return techCapabilityService.getTechCapabilityHistory(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/product/{id}")
    @Operation(summary = "Получение списка ТС которые реализованы в продукте и за которые система ответственна",
            description = "Получение списка ТС которые реализованы в продукте и за которые система ответственна",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = ResponsibilityTcDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponsibilityTcDTO getTechCapabilityResp(@PathVariable Integer id) {
        return techCapabilityService.getTechCapabilityResp(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/history/compare/{id}/{version}")
    @Operation(summary = "Получение выбраных версий TC",
            description = "Получение выбраных версий TC",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<GetTcHistoryVersionDTO> getTechCapabilityHistoryVersion(@PathVariable Long id,
                                                                        @PathVariable Integer version,
                                                                        @RequestParam(value = "other_version",
                                                                                required = false) Integer otherVersion) {
        return techCapabilityService.getTechCapabilityHistoryVersion(id, version, otherVersion);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/list/by-ids")
    @Operation(summary = "получение списка технических возможностей",
            description = "получение списка технических возможностей",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity<List<ParentDTO>> getArrayTech(@RequestParam List<Long> ids) {
        return ResponseEntity.status(HttpStatus.OK).body(techCapabilityService.getArrayCapability(ids));
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/by-code")
    @Operation(summary = "получение списка id технической возможности по списку code",
            description = "получение списка id технической возможности по списку code",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<IdCodeDTO> getAllTechIdsByCodes(@RequestParam List<String> codes) {
        return techCapabilityService.getAllTechIdsByCodes(codes);
    }

    @ApiErrorCodes({400, 401, 403, 404, 409, 500})
    @PutMapping
    @Operation(summary = "Создание/Обновление технической возможности",
            description = "Создание/Обновление технической возможности",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity putTechCapability(@RequestBody PutTechCapabilityDTO techCapability,
                                            @RequestHeader(value = SOURCE, required = false) String source) {
        log.info("Receive Tech Capability:" + techCapability.toString());
        techCapabilityService.validateTechCapabilityDTO(techCapability);
        techCapabilityService.createOrUpdate(techCapability, source);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiErrorCodes({400, 401, 403, 404, 409, 500})
    @DeleteMapping("/{code}")
    @Operation(summary = "Удаление записи из таблицы find_name_sort_table со статусом TC",
            description = "Удаление записи из таблицы find_name_sort_table со статусом TC",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity deleteTechCapability(@PathVariable String code) {
        techCapabilityService.deleteTechCapability(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
