/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.SearchCapabilityDTO;
import ru.beeline.capability.dto.aitooldto.ResultDTO;
import ru.beeline.capability.service.SearchCapabilityService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1")
public class SearchCapabilityController {

    @Autowired
    private SearchCapabilityService searchCapabilityService;


    @ApiErrorCodes({400, 500})
    @GetMapping("/find")
    @Operation(summary = "Поиск по сущностям",
            description = "Поиск по сущностям",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = SearchCapabilityDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<SearchCapabilityDTO> getAllTech(@RequestParam(value = "findBy", required = false, defaultValue = "ALL") String findBy,
                                                @RequestParam(value = "search") String search) {
        return searchCapabilityService.searchCapability(search, findBy);
    }
}
