/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;


 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.annotation.AiToolHeaders;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.PostPromtDTO;
import ru.beeline.capability.dto.PromtDTO;
import ru.beeline.capability.dto.aitooldto.ResultDTO;
import ru.beeline.capability.service.PromtService;


@RestController
@RequestMapping("/api/v1/promt")
@Tag(name = "Промты", description = "Сохранённые промпты для AI")
public class PromtController {

    @Autowired
    private PromtService promtService;

    @ApiErrorCodes({400, 404, 500})
    @GetMapping("/{alias}")
    @Operation(summary = "Промт по alias",
            description = "Возвращает сохраненный промпт по alias.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = PromtDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден"),
            })
    public PromtDTO getPromtByAlias(@PathVariable String alias) {
        return promtService.getPromtByAlias(alias);
    }

    @AiToolHeaders
    @ApiErrorCodes({400, 404, 500})
    @PostMapping("/proxy")
    @Operation(summary = "Проксирование запроса в LLM с использованием сохраненного промпта",
            description = "Отправляет запрос в LLM, применяя сохраненный промпт и параметры из тела запроса.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = ResultDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден"),
            })
    public ResponseEntity<ResultDTO> getPromtProxy(@RequestBody PostPromtDTO postPromtDTO) {
        return ResponseEntity.ok(promtService.postPromtProxy(postPromtDTO));
    }
}
