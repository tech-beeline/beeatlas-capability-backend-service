/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.*;
import ru.beeline.capability.service.BusinessCapabilityService;
import ru.beeline.capability.dto.BusinessCapabilityChildrenDTO;
import ru.beeline.capability.dto.BusinessCapabilityChildrenIdsDTO;
import ru.beeline.capability.dto.PutBusinessCapabilityDTO;

import java.util.Collections;
import java.util.List;

import static ru.beeline.capability.utils.Constants.*;


@RestController
@RequestMapping("/api/v1/business-capability")
public class BusinessCapabilityController {

    @Autowired
    private BusinessCapabilityService businessCapabilityService;

    @ApiErrorCodes({400, 500})
    @GetMapping("/{id}/children")
    @Operation(summary = "Получение дочерних бизнес-возможностей",
            description = "Возвращает список дочерних бизнес-возможностей для указанной.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = BusinessCapabilityChildrenDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
            })
    public BusinessCapabilityChildrenDTO getKidsById(@PathVariable Long id) {
        return businessCapabilityService.getChildren(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/{id}/children/all")
    @Operation(summary = "Получение id всех дочерних бизнес-возможностей",
            description = "Возвращает идентификаторы всех дочерних бизнес-возможностей (включая вложенные).",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = BusinessCapabilityChildrenIdsDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
            })
    public BusinessCapabilityChildrenIdsDTO getAllKidsIdById(@PathVariable Long id) {
        return businessCapabilityService.getChildrenIds(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/{id}/parents")
    @Operation(summary = "Получение родительских бизнес-возможностей",
            description = "Возвращает цепочку родителей для указанной бизнес-возможности.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = CapabilityParentDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
            })
    public CapabilityParentDTO getParentsById(@PathVariable Long id) {
        CapabilityParentDTO capabilityParentDTO = businessCapabilityService.getParentsWithoutDeleteDate(id);
        Collections.reverse(capabilityParentDTO.getParents());
        return capabilityParentDTO;
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/{id}")
    @Operation(summary = "Получение бизнес возможности по идентификатору",
            description = "Возвращает бизнес-возможность по её id.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = BusinessCapabilityShortDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
            })
    public BusinessCapabilityShortDTO getById(@PathVariable Long id) {
        return businessCapabilityService.getById(id);
    }
    @ApiErrorCodes({400, 500})
    @GetMapping("/tree")
    @Operation(summary = "Построение дерева",
            description = "Возвращает дерево бизнес-возможностей.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BusinessCapabilityTreeDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
            })
    public List<BusinessCapabilityTreeDTO> getBusinessCapabilityTree() {
        return businessCapabilityService.getBusinessCapabilityTree();
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/tree/{id}")
    @Operation(summary = "Построение дерева по идентификатору возможности",
            description = "Возвращает дерево, построенное от указанной бизнес-возможности.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = BusinessCapabilityTreeCustomDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
            })
    public BusinessCapabilityTreeCustomDTO getBusinessCapabilityTreeById(@PathVariable Long id) {
        return businessCapabilityService.getBusinessCapabilityTreeById(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping
    @Operation(summary = "Получение бизнес возможностей",
            description = "Возвращает список бизнес-возможностей с учетом фильтров и пагинации.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BusinessCapabilityShortDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
            })
    public List<BusinessCapabilityShortDTO> getBusinessCapabilities(@RequestParam(value = "limit", required = false) Integer limit,
                                                                    @RequestParam(value = "findBy", required = false, defaultValue = "ALL") String findBy,
                                                                    @RequestParam(value = "offset", required = false) Integer offset) {
        return businessCapabilityService.getCapabilities(limit, offset, findBy);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/history/{id}")
    @Operation(summary = "Получение списка версий ВС",
            description = "Возвращает историю версий бизнес-возможности.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetHistoryByIdDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
            })
    public List<GetHistoryByIdDTO> getBusinessCapabilityHistory(@PathVariable Long id) {
        return businessCapabilityService.getBusinessCapabilityHistory(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/history/compare/{id}/{version}")
    @Operation(summary = "Получение выбраных версий ВС",
            description = "Возвращает сравнение указанной версии с другой (при наличии параметра).",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetBcHistoryVersionDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
            })
    public List<GetBcHistoryVersionDTO> getBusinessCapabilityHistoryVersion(@PathVariable Long id,
                                                                            @PathVariable Integer version,
                                                                            @RequestParam(value = "other_version", required = false) Integer otherVersion) {
        return businessCapabilityService.getBusinessCapabilityHistoryVersion(id, version, otherVersion);
    }

    @ApiErrorCodes({400, 401, 403, 404, 409, 500})
    @PutMapping
    @Operation(summary = "Создание/Обновление бизнес возможности",
            description = "Создает или обновляет бизнес-возможность по переданным данным.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content()),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
            })
    public ResponseEntity putBusinessCapability(@RequestBody PutBusinessCapabilityDTO capability,
                                                @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                                @RequestHeader(value = USER_PRODUCTS_IDS_HEADER, required = false) String productIds,
                                                @RequestHeader(value = USER_ROLES_HEADER, required = false) String roles,
                                                @RequestHeader(value = USER_PERMISSION_HEADER, required = false) String permissions,
                                                @RequestHeader(value = SOURCE, required = false) String source) {
        businessCapabilityService.validateBusinessCapabilityDTO(capability, userId, productIds, roles, permissions);
        businessCapabilityService.putCapability(capability, userId, productIds, roles, permissions, source);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiErrorCodes({400, 500})
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удаление бизнес-возможности",
            description = "Удаляет бизнес-возможность по коду (корневую удалить нельзя).",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Без содержимого"),
                    @ApiResponse(responseCode = "400", description = "Выбранный business capability является корневым"),
            })
    public ResponseEntity<Void> deleteBusinessCapability(@PathVariable String code,
            @RequestParam(value = "children-transfer", required = false) Boolean childrenTransfer) {
        businessCapabilityService.deleteBusinessCapability(code, childrenTransfer);
        return ResponseEntity.noContent().build();
    }

    @ApiErrorCodes({400, 404, 500})
    @PostMapping("/public/{id}")
    @Operation(summary = "Публикация ВС",
            description = "Публикует бизнес-возможность по id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @ApiResponse(responseCode = "404", description = "Продукт не найден"),
            })
    public ResponseEntity<Void> postBusinessCapability(@PathVariable Integer id) {
        businessCapabilityService.postBusinessCapability(id);
        return new ResponseEntity<>(HttpStatus.OK);

    }
}
