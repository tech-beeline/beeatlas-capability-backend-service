/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.PostBusinessCapabilityDTO;
import ru.beeline.capability.dto.PostTechCapabilityDTO;
import ru.beeline.capability.dto.PackageRegistrationResponseDTO;
import ru.beeline.capability.exception.PackageRegistrationException;
import ru.beeline.capability.service.PackageCapabilityService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class PackageCapabilityController {

    @Autowired
    private PackageCapabilityService packageCapabilityService;

    @ApiErrorCodes({400, 500})
    @PostMapping("/package-tech-capabilities")
    @Operation(summary = "Пакетная загрузка технических возможностей",
            description = "Регистрирует пакет технических возможностей одним запросом.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = PackageRegistrationResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity<PackageRegistrationResponseDTO> packLoadTechCapabilities(@RequestBody List<PostTechCapabilityDTO> techCapabilities) {
        log.info("Receive Tech Capability:" + techCapabilities.toString());
        PackageRegistrationResponseDTO registeredCapabilityPackageInfo = packageCapabilityService.registerTechCapabilitiesPackage(techCapabilities);
        if(registeredCapabilityPackageInfo == null) throw new PackageRegistrationException("Tech capability package was not be registered");
        return new ResponseEntity<>(registeredCapabilityPackageInfo, HttpStatus.OK);
    }

    @ApiErrorCodes({400, 500})
    @PostMapping("/package-business-capabilities")
    @Operation(summary = "Пакетная загрузка бизнес возможностей",
            description = "Регистрирует пакет бизнес-возможностей одним запросом.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = PackageRegistrationResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity<PackageRegistrationResponseDTO> packLoadBusinessCapabilities(@RequestBody List<PostBusinessCapabilityDTO> businessCapabilities) {
        PackageRegistrationResponseDTO registeredCapabilityPackageInfo = packageCapabilityService.registerBusinessCapabilitiesPackage(businessCapabilities);
        if(registeredCapabilityPackageInfo == null) throw new PackageRegistrationException("Business capability package was not be registered");
        return new ResponseEntity<>(registeredCapabilityPackageInfo, HttpStatus.OK);
    }
}
