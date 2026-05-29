/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.repository.OrderBusinessCapabilityRepository;

import java.util.Map;

@RestController
public class InternalCheckController {

    @Autowired
    private OrderBusinessCapabilityRepository orderRepository;

    @GetMapping("/api/v1/internal/check/bc-order-draft/{id}/owner")
    public ResponseEntity<Map<String, Boolean>> checkBcOrderDraftOwner(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        return orderRepository.findById(id)
                .map(order -> order.getOrderOwnerId() != null
                        && order.getOrderOwnerId().equals(userId))
                .map(hasAccess -> ResponseEntity.ok(Map.of("hasAccess", hasAccess)))
                .orElseThrow(() -> new NotFoundException("BC order draft not found: " + id));
    }
}
