/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;


 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.annotation.AiToolHeaders;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.PostPromtDTO;
import ru.beeline.capability.dto.PromtDTO;
import ru.beeline.capability.dto.aitooldto.ResultDTO;
import ru.beeline.capability.service.PromtService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/promt")
public class PromtController {

    @Autowired
    private PromtService promtService;

    @ApiErrorCodes({400, 404, 500})
    @GetMapping("/{alias}")
    @Operation(summary = "Промт по alias",
            description = "Промт по alias",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = PromtDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public PromtDTO getPromtByAlias(@PathVariable String alias) {
        return promtService.getPromtByAlias(alias);
    }

    @AiToolHeaders
    @ApiErrorCodes({400, 404, 500})
    @PostMapping("/proxy")
    @Operation(summary = "Проксирования запроса в LLM с использованием сохраненного промота",
            description = "Проксирования запроса в LLM с использованием сохраненного промота",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = ResultDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity<ResultDTO> getPromtProxy(@RequestBody PostPromtDTO postPromtDTO) {
        return ResponseEntity.ok(promtService.postPromtProxy(postPromtDTO));
    }
}
