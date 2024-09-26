package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.dto.PostCapabilityMapDTO;
import ru.beeline.capability.service.CapabilityMapService;

import static ru.beeline.capability.utils.Constants.USER_ID_HEADER;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/maps")
public class CapabilityMapController {

    @Autowired
    private CapabilityMapService capabilityMapService;

    @PostMapping()
    @ApiOperation(value = "Создание карты")
    public ResponseEntity createCapabilityMap(@RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                              @RequestBody PostCapabilityMapDTO postCapabilityMapDTO) {
        capabilityMapService.createCapabilityMap(postCapabilityMapDTO, userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
