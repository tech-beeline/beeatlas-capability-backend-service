package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.dto.ShortCapabilityMapDTO;
import ru.beeline.capability.dto.PatchCapabilityMapDTO;
import ru.beeline.capability.dto.PostCapabilityMapDTO;
import ru.beeline.capability.service.CapabilityMapService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @PatchMapping("/{mapId}")
    @ApiOperation(value = "Обновления карты пользователя")
    public ResponseEntity patchCapabilityMap(@PathVariable Integer mapId,
                                              @RequestBody List<PatchCapabilityMapDTO> patchCapabilityMapDTO,
                                              HttpServletRequest request) {
        capabilityMapService.patchCapabilityMap(mapId, patchCapabilityMapDTO, request.getHeader(USER_ID_HEADER));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{mapId}")
    @ApiOperation(value = "Удаление карты пользователя")
    public ResponseEntity deleteCapabilityMap(@PathVariable Integer mapId, HttpServletRequest request) {
        capabilityMapService.deleteCapabilityMap(mapId, request.getHeader(USER_ID_HEADER));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    @ApiOperation(value = "Получение всех карт пользователя")
    public List<ShortCapabilityMapDTO> getCapabilityMaps(HttpServletRequest request) {
        return capabilityMapService.getCapabilityMaps(request.getHeader(USER_ID_HEADER));
    }
}