package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.dto.PostTechCapabilityDTO;
import ru.beeline.capability.dto.RegisteredCapabilityPackageDTO;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/")
public class PackageCapabilityController {

    @PostMapping("/package-tech-capabilities")
    @ApiOperation(value = "Изменение бизнес возможности")
    public ResponseEntity<RegisteredCapabilityPackageDTO> getBusinessCapabilities(@RequestBody List<PostTechCapabilityDTO> techCapabilities) {
        return new ResponseEntity(HttpStatus.OK);
    }
}