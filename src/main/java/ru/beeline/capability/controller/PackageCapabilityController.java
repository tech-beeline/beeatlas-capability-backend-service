package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.dto.PostTechCapabilityDTO;
import ru.beeline.capability.dto.RegisteredCapabilityPackageDTO;
import ru.beeline.capability.service.PackageCapabilityService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/")
public class PackageCapabilityController {

    @Autowired
    private PackageCapabilityService packageCapabilityService;

    @PostMapping("/package-tech-capabilities")
    @ApiOperation(value = "Изменение бизнес возможности")
    public ResponseEntity<RegisteredCapabilityPackageDTO> packLoadTechCapabilities(@RequestBody List<PostTechCapabilityDTO> techCapabilities) {
        RegisteredCapabilityPackageDTO registeredCapabilityPackageInfo = packageCapabilityService.registerTechCapabilitiesPackage(techCapabilities);

        return new ResponseEntity<>(registeredCapabilityPackageInfo, HttpStatus.OK);
    }
}