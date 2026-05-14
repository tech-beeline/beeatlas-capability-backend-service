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
@RestController
@RequestMapping("/api/v1/tech-capabilities")
@Tag(name = "Технические возможности", description = "Операции с техническими возможностями (TC)")
public class TechCapabilityController {

    @Autowired
    private TechCapabilityService techCapabilityService;

    @ApiErrorCodes({400, 500})
    @GetMapping
    @Operation(summary = "Получение технических возможностей",
            description = "Возвращает список технических возможностей с пагинацией.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TechCapabilityDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<TechCapabilityDTO> getTechCapabilities(@RequestParam(value = "limit", required = false) Integer limit,
                                                       @RequestParam(value = "offset", required = false) Integer offset) {
        return techCapabilityService.getCapabilities(limit, offset);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/{id}")
    @Operation(summary = "Получение технической возможности",
            description = "Возвращает техническую возможность по её id.",
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
            description = "Возвращает цепочку родителей для указанной технической возможности.",
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
            description = "Возвращает историю версий технической возможности.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetHistoryByIdDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<GetHistoryByIdDTO> getTechCapabilityHistory(@PathVariable Long id) {
        return techCapabilityService.getTechCapabilityHistory(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/product/{id}")
    @Operation(summary = "Получение списка ТС которые реализованы в продукте и за которые система ответственна",
            description = "Возвращает ТС, реализованные в продукте, за которые система ответственна.",
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
            description = "Возвращает сравнение указанной версии с другой (при наличии параметра).",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetTcHistoryVersionDTO.class)))),
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
    @Operation(summary = "Получение списка технических возможностей",
            description = "Возвращает технические возможности по списку id.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ParentDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity<List<ParentDTO>> getArrayTech(@RequestParam List<Long> ids) {
        return ResponseEntity.status(HttpStatus.OK).body(techCapabilityService.getArrayCapability(ids));
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/by-code")
    @Operation(summary = "Получение списка id технических возможностей по списку code",
            description = "Возвращает id технических возможностей по списку кодов.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = IdCodeDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<IdCodeDTO> getAllTechIdsByCodes(@RequestParam List<String> codes) {
        return techCapabilityService.getAllTechIdsByCodes(codes);
    }

    @ApiErrorCodes({400, 401, 403, 404, 409, 500})
    @PutMapping
    @Operation(summary = "Создание/Обновление технической возможности",
            description = "Создает или обновляет техническую возможность по переданным данным.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
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
    @Operation(summary = "Удаление технической возможности",
            description = "Удаляет техническую возможность по коду.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity deleteTechCapability(@PathVariable String code) {
        techCapabilityService.deleteTechCapability(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
