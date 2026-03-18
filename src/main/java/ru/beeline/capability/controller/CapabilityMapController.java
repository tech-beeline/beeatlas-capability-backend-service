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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.*;
import ru.beeline.capability.service.CapabilityMapService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static ru.beeline.capability.utils.Constants.USER_ID_HEADER;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class CapabilityMapController {

    @Autowired
    private CapabilityMapService capabilityMapService;

    @ApiErrorCodes({400, 500})
    @PostMapping("/v1/maps")
    @Operation(summary = "Создание карты возможностей",
            description = "Создание карты возможностей",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Создано",
                            content = @Content(schema = @Schema(implementation = CreateCapabilityMapResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity<CreateCapabilityMapResponseDTO> createCapabilityMap(@RequestBody PostCapabilityMapDTO postCapabilityMapDTO,
                                                                              HttpServletRequest request) {
        return new ResponseEntity<>(capabilityMapService.createCapabilityMap(postCapabilityMapDTO,
                request.getHeader(USER_ID_HEADER)), HttpStatus.CREATED);
    }

    @ApiErrorCodes({400, 401, 403, 404, 409, 500})
    @PatchMapping("/v1/maps/groups/{mapId}")
    @Operation(summary = "Обновление карты пользователя",
            description = "Обновление карты пользователя",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content()),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity patchCapabilityMap(@PathVariable Integer mapId,
                                             @RequestBody List<PatchCapabilityMapDTO> patchCapabilityMapDTO,
                                             HttpServletRequest request) {
        capabilityMapService.patchCapabilityMap(mapId, patchCapabilityMapDTO, request.getHeader(USER_ID_HEADER));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiErrorCodes({400, 401, 403, 404, 409, 500})
    @DeleteMapping("/v1/maps/{mapId}")
    @Operation(summary = "Удаление карты пользователя",
            description = "Удаление карты пользователя",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content()),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity deleteCapabilityMap(@PathVariable Integer mapId, HttpServletRequest request) {
        capabilityMapService.deleteCapabilityMap(mapId, request.getHeader(USER_ID_HEADER));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/v1/maps/{Id}")
    @Operation(summary = "Получение карты по id",
            description = "Получение карты по id",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = GetCapabilityMapByIdDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public GetCapabilityMapByIdDTO getCapabilityMapById(@PathVariable Integer Id) {
        return capabilityMapService.getCapabilityMapById(Id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/v1/maps")
    @Operation(summary = "Получение всех карт пользователя",
            description = "Получение всех карт пользователя",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ShortCapabilityMapDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<ShortCapabilityMapDTO> getCapabilityMaps(HttpServletRequest request) {
        return capabilityMapService.getCapabilityMaps(request.getHeader(USER_ID_HEADER));
    }

    @ApiErrorCodes({400, 401, 403, 404, 409, 500})
    @PatchMapping("/v1/maps/{mapId}")
    @Operation(summary = "Изменение названия и описания карты",
            description = "Изменение названия и описания карты",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content()),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity patchNameAndDescriptionCapabilityMap(@PathVariable Integer mapId,
                                                               @RequestBody NameAndDescriptionDTO nameAndDescriptionDTO,
                                                               HttpServletRequest request) {
        capabilityMapService.patchNameAndDescriptionCapabilityMap(mapId, nameAndDescriptionDTO, request.getHeader(USER_ID_HEADER));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
