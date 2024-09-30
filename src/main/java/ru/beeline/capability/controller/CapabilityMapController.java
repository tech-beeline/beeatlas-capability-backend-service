package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.dto.PostCapabilityMapDTO;
import ru.beeline.capability.service.CapabilityMapService;

import javax.servlet.http.HttpServletRequest;

import static ru.beeline.capability.utils.Constants.USER_ID_HEADER;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/maps")
public class CapabilityMapController {

    @Autowired
    private CapabilityMapService capabilityMapService;

    @PostMapping()
    @ApiOperation(value = "Создание карты возможностей")
    public ResponseEntity createCapabilityMap(@RequestBody PostCapabilityMapDTO postCapabilityMapDTO,
                                              HttpServletRequest request) {
        capabilityMapService.createCapabilityMap(postCapabilityMapDTO, request.getHeader(USER_ID_HEADER));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
