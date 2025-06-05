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
import ru.beeline.capability.dto.CreateCapabilityMapResponseDTO;
import ru.beeline.capability.dto.GetCapabilityMapByIdDTO;
import ru.beeline.capability.dto.NameAndDescriptionDTO;
import ru.beeline.capability.dto.PatchCapabilityMapDTO;
import ru.beeline.capability.dto.PostCapabilityMapDTO;
import ru.beeline.capability.dto.ShortCapabilityMapDTO;
import ru.beeline.capability.service.CapabilityMapService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static ru.beeline.capability.utils.Constants.USER_ID_HEADER;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class CapabilityMapController {

    @Autowired
    private CapabilityMapService capabilityMapService;

    @PostMapping("/v1/maps")
    @ApiOperation(value = "Создание карты возможностей")
    public ResponseEntity<CreateCapabilityMapResponseDTO> createCapabilityMap(@RequestBody PostCapabilityMapDTO postCapabilityMapDTO,
                                                                              HttpServletRequest request) {
        return new ResponseEntity<>(capabilityMapService.createCapabilityMap(postCapabilityMapDTO,
                request.getHeader(USER_ID_HEADER)), HttpStatus.CREATED);
    }

    @PatchMapping("/v1/maps/groups/{mapId}")
    @ApiOperation(value = "Обновления карты пользователя")
    public ResponseEntity patchCapabilityMap(@PathVariable Integer mapId,
                                             @RequestBody List<PatchCapabilityMapDTO> patchCapabilityMapDTO,
                                             HttpServletRequest request) {
        capabilityMapService.patchCapabilityMap(mapId, patchCapabilityMapDTO, request.getHeader(USER_ID_HEADER));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/v1/maps/{mapId}")
    @ApiOperation(value = "Удаление карты пользователя")
    public ResponseEntity deleteCapabilityMap(@PathVariable Integer mapId, HttpServletRequest request) {
        capabilityMapService.deleteCapabilityMap(mapId, request.getHeader(USER_ID_HEADER));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/v1/maps/{Id}")
    @ApiOperation(value = "Получение карты по id")
    public GetCapabilityMapByIdDTO getCapabilityMapById(@PathVariable Integer Id) {
        return capabilityMapService.getCapabilityMapById(Id);
    }

    @GetMapping("/v1/maps")
    @ApiOperation(value = "Получение всех карт пользователя")
    public List<ShortCapabilityMapDTO> getCapabilityMaps(HttpServletRequest request) {
        return capabilityMapService.getCapabilityMaps(request.getHeader(USER_ID_HEADER));
    }

    @PatchMapping("/v1/maps/{mapId}")
    @ApiOperation(value = "Изменение названия и описания карты")
    public ResponseEntity patchNameAndDescriptionCapabilityMap(@PathVariable Integer mapId,
                                                               @RequestBody NameAndDescriptionDTO nameAndDescriptionDTO,
                                                               HttpServletRequest request) {
        capabilityMapService.patchNameAndDescriptionCapabilityMap(mapId, nameAndDescriptionDTO, request.getHeader(USER_ID_HEADER));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}