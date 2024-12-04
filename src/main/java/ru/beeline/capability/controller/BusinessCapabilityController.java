package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.dto.BusinessCapabilityShortDTO;
import ru.beeline.capability.dto.BusinessCapabilityTreeCustomDTO;
import ru.beeline.capability.dto.BusinessCapabilityTreeDTO;
import ru.beeline.capability.dto.CapabilityParentDTO;
import ru.beeline.capability.dto.GetBcHistoryVersionDTO;
import ru.beeline.capability.dto.GetHistoryByIdDTO;
import ru.beeline.capability.service.BusinessCapabilityService;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenDTO;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenIdsDTO;
import ru.beeline.fdmlib.dto.capability.PutBusinessCapabilityDTO;

import java.util.Collections;
import java.util.List;

import static ru.beeline.capability.utils.Constants.USER_ID_HEADER;
import static ru.beeline.capability.utils.Constants.USER_PERMISSION_HEADER;
import static ru.beeline.capability.utils.Constants.USER_PRODUCTS_IDS_HEADER;
import static ru.beeline.capability.utils.Constants.USER_ROLES_HEADER;

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
    public List<BusinessCapabilityShortDTO> getBusinessCapabilities(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "findBy", required = false, defaultValue = "ALL") String findBy,
            @RequestParam(value = "offset", required = false) Integer offset) {
        return businessCapabilityService.getCapabilities(limit, offset, findBy);
    }

    @PutMapping
    @ApiOperation(value = "Создание/Обновление бизнес возможности")
    public ResponseEntity putBusinessCapability(@RequestBody PutBusinessCapabilityDTO capability,
                                                @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                                @RequestHeader(value = USER_PRODUCTS_IDS_HEADER, required = false) String productIds,
                                                @RequestHeader(value = USER_ROLES_HEADER, required = false) String roles,
                                                @RequestHeader(value = USER_PERMISSION_HEADER, required = false) String permissions
    ) {
        businessCapabilityService.validateBusinessCapabilityDTO(capability, userId, productIds, roles, permissions);
        businessCapabilityService.putCapability(capability, userId, productIds, roles, permissions);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{code}")
    @ApiOperation(value = "Удаление записи из таблицы find_name_sort_table со статусом BC")
    public ResponseEntity deleteBusinessCapability(@PathVariable String code) {
        businessCapabilityService.deleteBusinessCapability(code);
        return new ResponseEntity<>(HttpStatus.OK);
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
                                                                            @RequestParam(value = "other_version",
                                                                                    required = false) Integer otherVersion) {
        return businessCapabilityService.getBusinessCapabilityHistoryVersion(id, version, otherVersion);
    }
}
