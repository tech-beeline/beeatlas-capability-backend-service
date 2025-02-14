package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.dto.CapabilityExportDTO;
import ru.beeline.capability.service.CapabilityExportService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/export")
public class CapabilityExportController {

    @Autowired
    CapabilityExportService capabilityExportService;

    @GetMapping("/business-capability/{doc_id}")
    @ApiOperation(value = "get export Business Capabilities")
    public ResponseEntity<CapabilityExportDTO> getExportBusinessCapabilities(@PathVariable(name = "doc_id") Integer docId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(capabilityExportService.getExportBusinessCapabilities(docId));
    }
}
