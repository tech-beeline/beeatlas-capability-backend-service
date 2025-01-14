package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.dto.*;
import ru.beeline.capability.service.BusinessCapabilityService;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenDTO;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenIdsDTO;
import ru.beeline.fdmlib.dto.capability.PutBusinessCapabilityDTO;

import java.util.Collections;
import java.util.List;

import static ru.beeline.capability.utils.Constants.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/business-capability")
public class BusinessCapabilityController {

    @Autowired
    private BusinessCapabilityService businessCapabilityService;

    @GetMapping("/{id}/children")
    @ApiOperation(value = "Получение всех дочерних бизнес возможностей", response = BusinessCapabilityChildrenDTO.class)
    public BusinessCapabilityChildrenDTO getKidsById(@PathVariable Long id) {
        return businessCapabilityService.getChildren(id);
    }

    @GetMapping("/{id}/children/all")
    @ApiOperation(value = "Получение всех дочерних бизнес возможностей", response = BusinessCapabilityChildrenDTO.class)
    public BusinessCapabilityChildrenIdsDTO getAllKidsIdById(@PathVariable Long id) {
        return businessCapabilityService.getChildrenIds(id);
    }

    @GetMapping("/{id}/parents")
    @ApiOperation(value = "Получение всех родительских бизнес возможностей", response = CapabilityParentDTO.class)
    public CapabilityParentDTO getParentsById(@PathVariable Long id) {
        CapabilityParentDTO capabilityParentDTO = businessCapabilityService.getParentsWithoutDeleteDate(id);
        Collections.reverse(capabilityParentDTO.getParents());
        return capabilityParentDTO;
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Получение бизнес возможности по идентификатору", response = BusinessCapabilityShortDTO.class)
    public BusinessCapabilityShortDTO getById(@PathVariable Long id) {
        return businessCapabilityService.getById(id);
    }

    @GetMapping("/tree")
    @ApiOperation(value = "Построение дерева", response = List.class)
    public List<BusinessCapabilityTreeDTO> getBusinessCapabilityTree() {
        return businessCapabilityService.getBusinessCapabilityTree();
    }

    @GetMapping("/tree/{id}")
    @ApiOperation(value = "Построение дерева по идентификатору возможности", response = List.class)
    public BusinessCapabilityTreeCustomDTO getBusinessCapabilityTreeById(@PathVariable Long id) {
        return businessCapabilityService.getBusinessCapabilityTreeById(id);
    }

    @GetMapping
    @ApiOperation(value = "Получение бизнес возможностей")
    public List<BusinessCapabilityShortDTO> getBusinessCapabilities(@RequestParam(value = "limit", required = false) Integer limit,
                                                                    @RequestParam(value = "findBy", required = false, defaultValue = "ALL") String findBy,
                                                                    @RequestParam(value = "offset", required = false) Integer offset) {
        return businessCapabilityService.getCapabilities(limit, offset, findBy);
    }

    @GetMapping("/history/{id}")
    @ApiOperation(value = "Получение списка версий ВС")
    public List<GetHistoryByIdDTO> getBusinessCapabilityHistory(@PathVariable Long id) {
        return businessCapabilityService.getBusinessCapabilityHistory(id);
    }

    @GetMapping("/history/compare/{id}/{version}")
    @ApiOperation(value = "Получение выбраных версий BC")
    public List<GetBcHistoryVersionDTO> getBusinessCapabilityHistoryVersion(@PathVariable Long id,
                                                                            @PathVariable Integer version,
                                                                            @RequestParam(value = "other_version", required = false) Integer otherVersion) {
        return businessCapabilityService.getBusinessCapabilityHistoryVersion(id, version, otherVersion);
    }

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
    public ResponseEntity deleteBusinessCapability(@PathVariable String code) {
        businessCapabilityService.deleteBusinessCapability(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }



    @PostMapping("/public/{id}")
    @ApiOperation(value = "Публикация ВС")
    public ResponseEntity postBusinessCapability(@PathVariable Integer id) {
        businessCapabilityService.postBusinessCapability(id);
        return new ResponseEntity<>(HttpStatus.OK);

    }
}
