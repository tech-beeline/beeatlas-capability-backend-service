package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/business-capability")
public class BusinessCapabilityController {
//    @Autowired
//    private BusinessCapabilityService businessCapabilityService;
//
//    @GetMapping("/{id}/children")
//    @ApiOperation(value = "Получение всех дочерних бизнес возможностей", response = TechCapabilityDto.class)
//    public TechCapabilityDto getAllTech(@PathVariable Long id) {
//        return businessCapabilityService.getChildren(id);
//    }
}
