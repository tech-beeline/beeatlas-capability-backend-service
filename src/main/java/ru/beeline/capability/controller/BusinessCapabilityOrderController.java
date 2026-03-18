/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.annotation.ApiErrorCodes;
import ru.beeline.capability.dto.BusinessCapabilityOrderDomainDTO;
import ru.beeline.capability.dto.BusinessCapabilityOrderDraftRequestDTO;
import ru.beeline.capability.dto.BusinessCapabilityOrderPatchRequestDTO;
import ru.beeline.capability.dto.BusinessCapabilityOrderRequestDTO;
import ru.beeline.capability.dto.BusinessCapabilityOrderResponseDTO;
import ru.beeline.capability.service.BusinessCapabilityOrderService;
import ru.beeline.capability.dto.BusinessCapabilityOrderDraftResponseDTO;

import java.util.List;


@RestController
@RequestMapping("/api/v1/business-capability")
public class BusinessCapabilityOrderController {


    @Autowired
    private BusinessCapabilityOrderService orderService;

    @ApiErrorCodes({400, 500})
    @PostMapping("/order")
    @Operation(summary = "Публикация каталога Capability",
            description = "Публикация каталога Capability",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = BusinessCapabilityOrderResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity postOrder(@RequestBody BusinessCapabilityOrderRequestDTO request) {
        String businessKey = orderService.createOrder(request);
        return ResponseEntity.ok(new BusinessCapabilityOrderResponseDTO(businessKey));
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/order/draft")
    @Operation(summary = "Получение черновика",
            description = "Получение черновика",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BusinessCapabilityOrderDraftResponseDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<BusinessCapabilityOrderDraftResponseDTO> getBusinessCapabilityOrderDraft() {
        return orderService.getBusinessCapabilityDraft();
    }

    @ApiErrorCodes({400, 500})
    @GetMapping("/order/{id}")
    @Operation(summary = "Получение данных по идентификатору",
            description = "Получение данных по идентификатору",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = BusinessCapabilityOrderDraftResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public BusinessCapabilityOrderDraftResponseDTO getBusinessCapabilityOrderById(@PathVariable Integer id) {
        return orderService.getBusinessCapabilityOrderById(id);
    }

    @ApiErrorCodes({400, 500})
    @PostMapping("/order/draft")
    @Operation(summary = "Публикация черновика",
            description = "Публикация черновика",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity postOrderDraft(@RequestBody BusinessCapabilityOrderDraftRequestDTO request) {
        orderService.createOrderDraft(request);
        return ResponseEntity.ok("");
    }

    @ApiErrorCodes({400, 500})
    @PostMapping("/order/domains")
    @Operation(summary = "Информация о доменах по списку id",
            description = "Получение информации о доменах по списку id",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный ответ",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BusinessCapabilityOrderDomainDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public List<BusinessCapabilityOrderDomainDTO> postOrderDomains(@RequestBody List<Integer> ids) {
        return orderService.getOrderDomains(ids);
    }

    @ApiErrorCodes({400, 401, 403, 404, 409, 500})
    @PatchMapping("/order/{id}")
    @Operation(summary = "Управление каталогом Capability",
            description = "Управление каталогом Capability",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity patchOrder(@PathVariable Integer id,
                                     @RequestBody BusinessCapabilityOrderPatchRequestDTO request,
                                     @RequestParam(required = false) String statusAlias) {
        orderService.editOrder(id, request, statusAlias);
        return new ResponseEntity(HttpStatus.OK);
    }

    @ApiErrorCodes({400, 401, 403, 404, 409, 500})
    @PatchMapping("/order/draft/{id}")
    @Operation(summary = "Управление каталогом Capability",
            description = "Управление каталогом Capability",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            })
    public ResponseEntity patchOrderDraft(@PathVariable Integer id,
                                          @RequestBody BusinessCapabilityOrderRequestDTO request,
                                          @RequestParam(required = false) boolean publish) {
        orderService.editOrderDraft(id, request, publish);
        return new ResponseEntity(HttpStatus.OK);
    }
}
