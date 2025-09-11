package ru.beeline.capability.controller.v2;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.service.BusinessCapabilityService;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenDTOV2;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v2/business-capability")
public class BusinessCapabilityController {

    @Autowired
    private BusinessCapabilityService businessCapabilityService;

    @GetMapping("/{id}/children")
    @ApiOperation(value = "Получение всех дочерних бизнес возможностей", response = BusinessCapabilityChildrenDTOV2.class)
    public BusinessCapabilityChildrenDTOV2 getKidsById(@PathVariable Long id) {
        return businessCapabilityService.getChildrenV2(id);
    }
}
