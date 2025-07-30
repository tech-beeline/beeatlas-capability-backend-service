package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.service.TechCapabilityService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1")
public class TechCapabilityCalculateController {

    @Autowired
    private TechCapabilityService techCapabilityService;

    @PostMapping("/calculate-total-tech-capabilities")
    @ApiOperation(value = "Запустить процесс общего расчета критериев для тепловых карт")
    public ResponseEntity calculateTotalTechCapabilitiesCount() {
        techCapabilityService.calculateTotalTechCapabilitiesCount();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/tech-capability/recount-quality")
    @ApiOperation(value = "Вызова процесса пересчета качества описания ТС")
    public ResponseEntity getTechRecalculationProcess() {
        techCapabilityService.getTechRecalculationProcess();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
