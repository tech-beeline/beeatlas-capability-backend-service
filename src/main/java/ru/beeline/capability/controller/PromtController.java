package ru.beeline.capability.controller;


import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.PromtDTO;
import ru.beeline.capability.service.PromtService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/promt")
public class PromtController {

    @Autowired
    private PromtService promtService;

    @ApiErrorCodes({400, 404, 500})
    @GetMapping("/{alias}")
    @ApiOperation(value = "Промт по alias")
    public PromtDTO getPromtByAlias(@PathVariable String alias) {
        return promtService.getPromtByAlias(alias);
    }
}
