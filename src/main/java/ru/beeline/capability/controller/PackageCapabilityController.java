/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.dto.PostBusinessCapabilityDTO;
import ru.beeline.capability.dto.PostTechCapabilityDTO;
import ru.beeline.capability.dto.PackageRegistrationResponseDTO;
import ru.beeline.capability.exception.PackageRegistrationException;
import ru.beeline.capability.service.PackageCapabilityService;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1")
public class PackageCapabilityController {

    @Autowired
    private PackageCapabilityService packageCapabilityService;

    @PostMapping("/package-tech-capabilities")
    @ApiOperation(value = "Пакетная загрузка технических возможностей")
    public ResponseEntity<PackageRegistrationResponseDTO> packLoadTechCapabilities(@RequestBody List<PostTechCapabilityDTO> techCapabilities) {
        log.info("Receive Tech Capability:" + techCapabilities.toString());
        PackageRegistrationResponseDTO registeredCapabilityPackageInfo = packageCapabilityService.registerTechCapabilitiesPackage(techCapabilities);
        if(registeredCapabilityPackageInfo == null) throw new PackageRegistrationException("Tech capability package was not be registered");
        return new ResponseEntity<>(registeredCapabilityPackageInfo, HttpStatus.OK);
    }

    @PostMapping("/package-business-capabilities")
    @ApiOperation(value = "Пакетная загрузка бизнес возможностей")
    public ResponseEntity<PackageRegistrationResponseDTO> packLoadBusinessCapabilities(@RequestBody List<PostBusinessCapabilityDTO> businessCapabilities) {
        PackageRegistrationResponseDTO registeredCapabilityPackageInfo = packageCapabilityService.registerBusinessCapabilitiesPackage(businessCapabilities);
        if(registeredCapabilityPackageInfo == null) throw new PackageRegistrationException("Business capability package was not be registered");
        return new ResponseEntity<>(registeredCapabilityPackageInfo, HttpStatus.OK);
    }
}