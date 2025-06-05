package ru.beeline.capability.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.capability.dto.BusinessCapabilityOrderDraftRequestDTO;
import ru.beeline.capability.dto.BusinessCapabilityOrderPatchRequestDTO;
import ru.beeline.capability.dto.BusinessCapabilityOrderRequestDTO;
import ru.beeline.capability.dto.BusinessCapabilityOrderResponseDTO;
import ru.beeline.capability.service.BusinessCapabilityOrderService;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityOrderDraftResponseDTO;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/business-capability")
public class BusinessCapabilityOrderController {


    @Autowired
    private BusinessCapabilityOrderService orderService;

    @PostMapping("/order")
    @ApiOperation(value = "Публикация каталога Capability")
    public ResponseEntity postOrder(@RequestBody BusinessCapabilityOrderRequestDTO request) {
        String businessKey = orderService.createOrder(request);
        return ResponseEntity.ok(new BusinessCapabilityOrderResponseDTO(businessKey));
    }

    @GetMapping("/order/draft")
    @ApiOperation(value = "Получение черновика", response = List.class)
    public List<BusinessCapabilityOrderDraftResponseDTO> getBusinessCapabilityOrderDraft() {
        return orderService.getBusinessCapabilityDraft();
    }

    @GetMapping("/order/{id}")
    @ApiOperation(value = "Получение данных по идентификатору", response = BusinessCapabilityOrderDraftResponseDTO.class)
    public BusinessCapabilityOrderDraftResponseDTO getBusinessCapabilityOrderById(@PathVariable Integer id) {
        return orderService.getBusinessCapabilityOrderById(id);
    }

    @PostMapping("/order/draft")
    @ApiOperation(value = "Публикация черновика")
    public ResponseEntity postOrderDraft(@RequestBody BusinessCapabilityOrderDraftRequestDTO request) {
        orderService.createOrderDraft(request);
        return ResponseEntity.ok("");
    }

    @PatchMapping("/order/{id}")
    @ApiOperation(value = "Управление каталогом Capability")
    public ResponseEntity patchOrder(@PathVariable Integer id,
                                     @RequestBody BusinessCapabilityOrderPatchRequestDTO request,
                                     @RequestParam(required = false) String statusAlias) {
        orderService.editOrder(id, request, statusAlias);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PatchMapping("/order/draft/{id}")
    @ApiOperation(value = "Управление каталогом Capability")
    public ResponseEntity patchOrderDraft(@PathVariable Integer id,
                                          @RequestBody BusinessCapabilityOrderRequestDTO request,
                                          @RequestParam(required = false) boolean publish) {
        orderService.editOrderDraft(id, request, publish);
        return new ResponseEntity(HttpStatus.OK);
    }
}
