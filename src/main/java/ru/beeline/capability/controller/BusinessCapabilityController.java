/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/business-capability")
public class BusinessCapabilityController {

    @Autowired
    private BusinessCapabilityService businessCapabilityService;

    @ApiErrorCodes({400, 500})
    @GetMapping("/{id}/children")
    @ApiOperation(value = "Получение всех дочерних бизнес возможностей", response = BusinessCapabilityChildrenDTO.class)
    public BusinessCapabilityChildrenDTO getKidsById(@PathVariable Long id) {
        return businessCapabilityService.getChildren(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/{id}/children/all")
    @ApiOperation(value = "Получение всех дочерних бизнес возможностей", response = BusinessCapabilityChildrenDTO.class)
    public BusinessCapabilityChildrenIdsDTO getAllKidsIdById(@PathVariable Long id) {
        return businessCapabilityService.getChildrenIds(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/{id}/parents")
    @ApiOperation(value = "Получение всех родительских бизнес возможностей", response = CapabilityParentDTO.class)
    public CapabilityParentDTO getParentsById(@PathVariable Long id) {
        CapabilityParentDTO capabilityParentDTO = businessCapabilityService.getParentsWithoutDeleteDate(id);
        Collections.reverse(capabilityParentDTO.getParents());
        return capabilityParentDTO;
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/{id}")
    @ApiOperation(value = "Получение бизнес возможности по идентификатору", response = BusinessCapabilityShortDTO.class)
    public BusinessCapabilityShortDTO getById(@PathVariable Long id) {
        return businessCapabilityService.getById(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/tree")
    @ApiOperation(value = "Построение дерева", response = List.class)
    public List<BusinessCapabilityTreeDTO> getBusinessCapabilityTree() {
        return businessCapabilityService.getBusinessCapabilityTree();
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/tree/{id}")
    @ApiOperation(value = "Построение дерева по идентификатору возможности", response = List.class)
    public BusinessCapabilityTreeCustomDTO getBusinessCapabilityTreeById(@PathVariable Long id) {
        return businessCapabilityService.getBusinessCapabilityTreeById(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping
    @ApiOperation(value = "Получение бизнес возможностей")
    public List<BusinessCapabilityShortDTO> getBusinessCapabilities(@RequestParam(value = "limit", required = false) Integer limit,
                                                                    @RequestParam(value = "findBy", required = false, defaultValue = "ALL") String findBy,
                                                                    @RequestParam(value = "offset", required = false) Integer offset) {
        return businessCapabilityService.getCapabilities(limit, offset, findBy);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/history/{id}")
    @ApiOperation(value = "Получение списка версий ВС")
    public List<GetHistoryByIdDTO> getBusinessCapabilityHistory(@PathVariable Long id) {
        return businessCapabilityService.getBusinessCapabilityHistory(id);
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/history/compare/{id}/{version}")
    @ApiOperation(value = "Получение выбраных версий BC")
    public List<GetBcHistoryVersionDTO> getBusinessCapabilityHistoryVersion(@PathVariable Long id,
                                                                            @PathVariable Integer version,
                                                                            @RequestParam(value = "other_version", required = false) Integer otherVersion) {
        return businessCapabilityService.getBusinessCapabilityHistoryVersion(id, version, otherVersion);
    }

    @ApiErrorCodes({400, 401, 403, 404, 409, 500})
    @PutMapping
    @ApiOperation(value = "Создание/Обновление бизнес возможности")
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

    @DeleteMapping("/{code}")
    @ApiOperation(value = "Удаление записи из таблицы find_name_sort_table со статусом BC")
    @ApiResponses({
            @ApiResponse(code = 204, message = "no content"),
            @ApiResponse(code = 400, message = "Выбранный business capability является корневым"),
            @ApiResponse(code = 400, message = "Неверный тип параметра children-transfer")
    })
    public ResponseEntity<Void> deleteBusinessCapability(
            @PathVariable String code,
            @RequestParam(value = "children-transfer", required = false) Boolean childrenTransfer) {

        businessCapabilityService.deleteBusinessCapability(code, childrenTransfer);
        return ResponseEntity.noContent().build();
    }

    @ApiErrorCodes({500})
    @ApiResponses(@ApiResponse(code = 404, message = "Продукт не найден"))
    @PostMapping("/public/{id}")
    @ApiOperation(value = "Публикация ВС")
    public ResponseEntity<Void> postBusinessCapability(@PathVariable Integer id) {
        businessCapabilityService.postBusinessCapability(id);
        return new ResponseEntity<>(HttpStatus.OK);

    }
}
