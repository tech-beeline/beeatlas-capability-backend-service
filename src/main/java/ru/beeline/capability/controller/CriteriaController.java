/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.domain.EnumCriteria;
import ru.beeline.capability.dto.criteria.CriteriaRecordResponseDTO;
import ru.beeline.capability.dto.criteria.PostCriteriaRecordDTO;
import ru.beeline.capability.dto.criteria.PutEnumCriteriaDTO;
import ru.beeline.capability.service.CriteriaService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static ru.beeline.capability.utils.Constants.USER_ROLES_HEADER;


@RestController
@RequestMapping("/api/v1/criterias")
@Tag(name = "Критерии", description = "Справочник enum_criterias и оценки по критериям для BC/TC")
public class CriteriaController {

    @Autowired
    private CriteriaService criteriaService;

    @ApiErrorCodes({400, 500})
    @GetMapping
    @Operation(summary = "Получение критериев",
            description = "Возвращает список критериев с учетом опционального фильтра.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EnumCriteria.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<EnumCriteria> getCriteriaList(@RequestParam(required = false) String filter) {
        return criteriaService.getCriteria(filter);
    }

    @ApiErrorCodes({400, 403, 500})
    @PutMapping
    @Parameter(name = USER_ROLES_HEADER,
            in = ParameterIn.HEADER,
            description = "Для метода требуется роль ADMINISTRATOR",
            example = "ADMINISTRATOR",
            required = true)
    @Operation(summary = "Создание или обновление критерия",
            description = "Создаёт запись в enum_criterias или обновляет существующую по name (без учёта регистра). Требуется роль ADMINISTRATOR.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = EnumCriteria.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            })
    public ResponseEntity<EnumCriteria> putCriteria(@RequestBody PutEnumCriteriaDTO body,
                                                    HttpServletRequest request) {
        return ResponseEntity.ok(criteriaService.upsertCriteria(body, request));
    }

    @ApiErrorCodes({400, 500})
    @PostMapping
    @Operation(summary = "Создание или обновление оценки по критерию для BC/TC",
            description = "Создаёт или обновляет запись в criterias_bc / criterias_tc. Заголовки авторизации не проверяются.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = CriteriaRecordResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity<CriteriaRecordResponseDTO> postCriteriaRecord(@RequestBody PostCriteriaRecordDTO body) {
        return ResponseEntity.ok(criteriaService.upsertCriteriaRecord(body));
    }

    @ApiErrorCodes({400, 403, 404, 500})
    @DeleteMapping("/{id}")
    @Parameter(name = USER_ROLES_HEADER,
            in = ParameterIn.HEADER,
            description = "Для метода требуется роль ADMINISTRATOR",
            example = "ADMINISTRATOR",
            required = true)
    @Operation(summary = "Удаление критерия",
            description = "Удаляет критерий из enum_criterias и связанные записи в criterias_tc и criterias_bc. Требуется роль ADMINISTRATOR.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Критерий удалён"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещён"),
                    @ApiResponse(responseCode = "404", description = "Критерий не найден"),
            })
    public ResponseEntity<Void> deleteCriteria(@PathVariable Long id, HttpServletRequest request) {
        criteriaService.deleteCriteria(id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
