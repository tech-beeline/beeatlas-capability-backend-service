package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.dto.CapabilityParentDTO;
import ru.beeline.capability.dto.GetHistoryByIdDTO;
import ru.beeline.capability.dto.GetTcHistoryVersionDTO;
import ru.beeline.capability.dto.IdCodeDTO;
import ru.beeline.capability.dto.ParentDTO;
import ru.beeline.capability.dto.TechCapabilityDTO;
import ru.beeline.capability.service.TechCapabilityService;
import ru.beeline.fdmlib.dto.capability.PutTechCapabilityDTO;

import java.util.List;

import static ru.beeline.capability.utils.Constants.SOURCE;

@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/tech-capabilities")
public class TechCapabilityController {

    @Autowired
    private TechCapabilityService techCapabilityService;

    @GetMapping
    @ApiOperation(value = "Получение технических возможностей")
    public List<TechCapabilityDTO> getTechCapabilities(@RequestParam(value = "limit", required = false) Integer limit,
                                                       @RequestParam(value = "offset", required = false) Integer offset) {
        return techCapabilityService.getCapabilities(limit, offset);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "получение технической возможности", response = TechCapabilityDTO.class)
    public TechCapabilityDTO getAllTech(@PathVariable Long id) {
        return techCapabilityService.getCapabilityById(id);
    }

    @GetMapping("/{id}/parents")
    @ApiOperation(value = "Получение всех родительских технических возможностей", response = CapabilityParentDTO.class)
    public CapabilityParentDTO getParentsById(@PathVariable Long id) {
        return techCapabilityService.getParents(id);
    }

    @GetMapping("/history/{id}")
    @ApiOperation(value = "Получение списка версий TC")
    public List<GetHistoryByIdDTO> getTechCapabilityHistory(@PathVariable Long id) {
        return techCapabilityService.getTechCapabilityHistory(id);
    }

    @GetMapping("/history/compare/{id}/{version}")
    @ApiOperation(value = "Получение выбраных версий TC")
    public List<GetTcHistoryVersionDTO> getTechCapabilityHistoryVersion(@PathVariable Long id,
                                                                        @PathVariable Integer version,
                                                                        @RequestParam(value = "other_version",
                                                                                required = false) Integer otherVersion) {
        return techCapabilityService.getTechCapabilityHistoryVersion(id, version, otherVersion);
    }

    @GetMapping("/list/by-ids")
    @ApiOperation(value = "получение списка технических возможностей")
    public ResponseEntity<List<ParentDTO>> getArrayTech(@RequestParam List<Long> ids) {
        return ResponseEntity.status(HttpStatus.OK).body(techCapabilityService.getArrayCapability(ids));
    }

    @GetMapping("/by-code")
    @ApiOperation(value = "получение списка id технической возможности по списку code")
    public List<IdCodeDTO> getAllTechIdsByCodes(@RequestParam List<String> codes) {
        return techCapabilityService.getAllTechIdsByCodes(codes);
    }

    @PutMapping
    @ApiOperation(value = "Создание/Обновление технической возможности")
    public ResponseEntity putTechCapability(@RequestBody PutTechCapabilityDTO techCapability,
                                            @RequestHeader(value = SOURCE, required = false) String source) {
        log.info("Receive Tech Capability:" + techCapability.toString());
        techCapabilityService.validateTechCapabilityDTO(techCapability);
        techCapabilityService.createOrUpdate(techCapability, source);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{code}")
    @ApiOperation(value = "Удаление записи из таблицы find_name_sort_table со статусом TC")
    public ResponseEntity deleteTechCapability(@PathVariable String code) {
        techCapabilityService.deleteTechCapability(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
