package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.dto.CapabilityParentDTO;
import ru.beeline.capability.dto.PutTechCapabilityDTO;
import ru.beeline.capability.dto.TechCapabilityDTO;
import ru.beeline.capability.service.TechCapabilityService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/tech-capabilities")
public class TechCapabilityController {

    @Autowired
    private TechCapabilityService techCapabilityService;

    @GetMapping
    @ApiOperation(value = "Получение технических возможностей")
    public List<TechCapabilityDTO> getTechCapabilities(@RequestParam(value = "limit", required = false) Integer limit,
                                                       @RequestParam(value = "offset", required = false) Integer offset) {
        return techCapabilityService.getCapabilities(limit, offset);
    }
    
    @GetMapping("/{id}")
    @ApiOperation(value = "получение технической возможности", response = TechCapabilityDTO.class)
    public TechCapabilityDTO getAllTech(@PathVariable Long id) {
        return techCapabilityService.getCapabilityById(id);
    }

    @GetMapping("/{id}/parents")
    @ApiOperation(value = "Получение всех родительских технических возможностей", response = CapabilityParentDTO.class)
    public CapabilityParentDTO getParentsById(@PathVariable Long id) {
        return techCapabilityService.getParents(id);
    }

    @PutMapping
    @ApiOperation(value = "Создание/Обновление технической возможности")
    public ResponseEntity putTechCapability(@RequestBody PutTechCapabilityDTO techCapability) {
        techCapabilityService.createOrUpdate(techCapability);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
