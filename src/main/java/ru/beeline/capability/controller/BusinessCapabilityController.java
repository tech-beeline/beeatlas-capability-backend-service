package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.dto.CapabilityParentDTO;
import ru.beeline.capability.dto.BusinessCapabilityShortDTO;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenDTO;
import ru.beeline.fdmlib.dto.capability.PutBusinessCapabilityDTO;
import ru.beeline.capability.service.BusinessCapabilityService;

import java.util.Collections;
import java.util.List;

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

    @GetMapping("/{id}/parents")
    @ApiOperation(value = "Получение всех родительских бизнес возможностей", response = CapabilityParentDTO.class)
    public CapabilityParentDTO getParentsById(@PathVariable Long id) {
        CapabilityParentDTO capabilityParentDTO = businessCapabilityService.getParents(id);
        Collections.reverse(capabilityParentDTO.getParents());
        return capabilityParentDTO;
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Получение бизнес возможности по идентификатору", response = BusinessCapabilityShortDTO.class)
    public BusinessCapabilityShortDTO getById(@PathVariable Long id) {
        return businessCapabilityService.getById(id);
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
    public ResponseEntity putBusinessCapability(@RequestBody PutBusinessCapabilityDTO capability) {
        businessCapabilityService.validateBusinessCapabilityDTO(capability);
        businessCapabilityService.putCapability(capability);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
