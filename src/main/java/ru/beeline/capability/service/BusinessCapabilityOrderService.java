package ru.beeline.capability.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.client.BpmClient;
import ru.beeline.capability.controller.RequestContext;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.OrderBusinessCapability;
import ru.beeline.capability.dto.*;
import ru.beeline.capability.exception.ForbiddenException;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.exception.ValidationException;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.OrderBusinessCapabilityRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BusinessCapabilityOrderService {

    @Autowired
    private BusinessCapabilityRepository bcRepository;
    @Autowired
    private OrderBusinessCapabilityRepository orderBcRepository;
    @Autowired
    private BpmClient bpmClient;
    @Autowired
    private OrderBusinessCapabilityRepository orderBusinessCapabilityRepository;

    public List<BusinessCapabilityOrderDraftResponseDTO> getBusinessCapabilityDraft() {
        List<OrderBusinessCapability> orderBusinessCapabilities = orderBcRepository.findByOrderOwnerIdAndBusinessKeyIsNull(
                Integer.parseInt(RequestContext.getUserId()));

        return orderBusinessCapabilities.stream().map(order -> {
            return BusinessCapabilityOrderDraftResponseDTO.builder()
                    .name(order.getName())
                    .description(order.getDescription())
                    .createdDate(order.getCreatedDate())
                    .updateDate(order.getLastModifiedDate())
                    .owner(order.getOwner())
                    .parent(ParentOrMutableDTO.builder()
                                    .id(order.getParent().getId())
                                    .code(order.getParent().getCode())
                                    .name(order.getParent().getName())
                                    .build())
                    .author(order.getAuthor())
                    .mutable(ParentOrMutableDTO.builder()
                                     .id(order.getMutableBusinessCapability().getId())
                                     .code(order.getMutableBusinessCapability().getCode())
                                     .name(order.getMutableBusinessCapability().getName())
                                     .build())
                    .build();
        }).collect(Collectors.toList());
    }

    public void editOrderDraft(Integer id, BusinessCapabilityOrderRequestDTO request, Boolean publish) {
        OrderBusinessCapability orderBusinessCapability = orderBcRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("OrderBusinessCapability не найдена"));

        if (orderBusinessCapability.getOrderOwnerId() != Integer.parseInt(RequestContext.getUserId())) {
            throw new ForbiddenException("403 Forbidden");
        }
        if (orderBusinessCapability.getBusinessKey() == null) {
            throw new IllegalArgumentException("Не является черновиком");
        }

        Long mutableBcId = request.getMutableBcId();

        String code = null;
        if (mutableBcId != null) {
            log.info("search bc code");
            BusinessCapability mutableBc = bcRepository.findById(mutableBcId)
                    .orElseThrow(() -> new IllegalArgumentException("изменение не существующей BC"));
            code = mutableBc.getCode();
        } else if (mutableBcId == null && !Objects.nonNull(orderBusinessCapability.getMutableBcId())) {
            log.info("search maxId orderBc");
            Integer maxId = orderBcRepository.findMaxId();
            long nextId = maxId + 1;
            code = String.format("NEW.BC-%06d", nextId);
        }
        boolean isUpdated = false;
        if (request.getName() != null && !request.getName().isEmpty()) {
            orderBusinessCapability.setName(request.getName());
            isUpdated = true;
        }

        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            orderBusinessCapability.setDescription(request.getDescription());
            isUpdated = true;
        }
        if (request.getOwner() != null && !request.getOwner().isEmpty()) {
            orderBusinessCapability.setOwner(request.getOwner());
            isUpdated = true;
        }
        if (request.getParentId() != null) {
            orderBusinessCapability.setParentId(request.getParentId());
            isUpdated = true;
        }
        if (request.getAuthor() != null && !request.getAuthor().isEmpty()) {
            orderBusinessCapability.setAuthor(request.getAuthor());
            isUpdated = true;
        }
        if (request.getMutableBcId() != null) {
            orderBusinessCapability.setMutableBcId(request.getMutableBcId());
            isUpdated = true;
        }
        orderBusinessCapability.setCode(code);
        if (isUpdated) {
            log.info("save bc");
            orderBusinessCapability.setLastModifiedDate(LocalDateTime.now());
            orderBusinessCapability = orderBusinessCapabilityRepository.save(orderBusinessCapability);
        }
        if (publish) {
            log.info("search bc");
            bcRepository.findByIdAndDeletedDateIsNull(Long.parseLong(request.getParentId().toString()))
                    .orElseThrow(() -> new IllegalArgumentException("Указаная несуществующая родительская возможность"));
            if (!orderBusinessCapability.getMutableBusinessCapability().getCode().equals(code)) {
                throw new IllegalArgumentException("изменение не существующей BC");
            }

            Map<String, Object> variables = new HashMap<>();
            variables.put("authorId", Integer.parseInt(RequestContext.getUserId()));
            variables.put("type", mutableBcId == null ? "create_business_capability" : "update_business_capability");
            variables.put("comment", request.getComment() != null ? request.getComment() : "");
            variables.put("entityId", orderBusinessCapability.getId().intValue());
            variables.put("name", request.getName());
            String businessKey = code + "_" + System.currentTimeMillis();

            log.info("call bpm");
            bpmClient.startProcess(businessKey, variables);
            orderBusinessCapability.setBusinessKey(businessKey);
            orderBusinessCapabilityRepository.save(orderBusinessCapability);
        }
    }

    public void editOrder(Integer id, BusinessCapabilityOrderPatchRequestDTO request, String statusAlias) {
        OrderBusinessCapability orderBusinessCapability = orderBcRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("OrderBusinessCapability не найдена"));
        if (request.getParentId() != null) {
            BusinessCapability parentBusinessCapability = bcRepository.findById(Long.parseLong(request.getParentId()
                                                                                                       .toString()))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Родительская BC не найдена или не является доменной"));
        }
        orderBusinessCapability.setName(request.getName());
        orderBusinessCapability.setDescription(request.getDescription());
        orderBusinessCapability.setOwner(request.getOwner());
        orderBusinessCapability.setParentId(request.getParentId());
        orderBusinessCapability.setLastModifiedDate(LocalDateTime.now());
        orderBusinessCapabilityRepository.save(orderBusinessCapability);
        if (statusAlias != null) {
            bpmClient.editStatusProcess(request.getComment(), orderBusinessCapability.getBusinessKey(), statusAlias);
        }
    }

    public String createOrder(BusinessCapabilityOrderRequestDTO request) {
        log.info("validate request");
        validateRequest(request);
        Long mutableBcId = request.getMutableBcId();

        String code;
        if (mutableBcId != null) {
            log.info("search bc code");
            BusinessCapability mutableBc = bcRepository.findById(mutableBcId)
                    .orElseThrow(() -> new IllegalArgumentException("изменение не существующей BC"));
            code = mutableBc.getCode();
        } else {
            log.info("search maxId orderBc");
            Integer maxId = orderBcRepository.findMaxId();
            long nextId = maxId + 1;
            code = String.format("NEW.BC-%06d", nextId);
        }

        log.info("search bc");
        bcRepository.findByIdAndDeletedDateIsNull(Long.parseLong(request.getParentId().toString()))
                .orElseThrow(() -> new IllegalArgumentException("Указаная несуществующая родительская возможность"));

        String businessKey = code + "_" + System.currentTimeMillis();


        OrderBusinessCapability orderBc = OrderBusinessCapability.builder()
                .id(orderBcRepository.findMaxId() + 1)
                .code(code)
                .name(request.getName())
                .description(request.getDescription())
                .owner(request.getOwner())
                .author(request.getAuthor())
                .status("PROPOSED")
                .isDomain(false)
                .parentId(request.getParentId())
                .mutableBcId(mutableBcId)
                .createdDate(LocalDateTime.now())
                .businessKey(businessKey)
                .orderOwnerId(Integer.parseInt(RequestContext.getUserId()))
                .build();
        log.info("save bc");
        orderBc = orderBcRepository.save(orderBc);
        orderBcRepository.save(orderBc);

        Map<String, Object> variables = new HashMap<>();
        variables.put("authorId", Integer.parseInt(RequestContext.getUserId()));
        variables.put("type", mutableBcId == null ? "create_business_capability" : "update_business_capability");
        variables.put("comment", request.getComment() != null ? request.getComment() : "");
        variables.put("entityId", orderBc.getId().intValue());
        variables.put("name", request.getName());

        log.info("call bpm");
        bpmClient.startProcess(businessKey, variables);

        return businessKey;
    }

    private void validateRequest(BusinessCapabilityOrderRequestDTO request) {
        StringBuilder errMsg = new StringBuilder();
        if (request.getName() == null || request.getName().isEmpty()) {
            errMsg.append("Отсутствует обязательное поле name\n");
        }
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            errMsg.append("Отсутствует обязательное поле description\n");
        }
        if (request.getOwner() == null || request.getOwner().isEmpty()) {
            errMsg.append("Отсутствует обязательное поле owner\n");
        }
        if (request.getParentId() == null) {
            errMsg.append("Отсутствует обязательное поле parentId\n");
        }
        if (request.getAuthor() == null || request.getAuthor().isEmpty()) {
            errMsg.append("Отсутствует обязательное поле author\n");
        }
        if (!errMsg.toString().isEmpty()) {
            throw new ValidationException(errMsg.toString());
        }
    }

    public String createOrderDraft(BusinessCapabilityOrderDraftRequestDTO request) {
        log.info("validate request");
        if (Objects.isNull(request.getName()) || request.getName().isEmpty()) {
            throw new ValidationException("Отсутствует обязательное поле name");
        }

        Long mutableBcId = request.getMutableBcId();

        String code;
        if (mutableBcId != null) {
            log.info("search bc code");
            BusinessCapability mutableBc = bcRepository.findById(mutableBcId)
                    .orElseThrow(() -> new IllegalArgumentException("изменение не существующей BC"));
            code = mutableBc.getCode();
        } else {
            log.info("search maxId orderBc");
            Integer maxId = orderBcRepository.findMaxId();
            long nextId = maxId + 1;
            code = String.format("NEW.BC-%06d", nextId);
        }

        log.info("search bc");
        bcRepository.findByIdAndDeletedDateIsNull(Long.parseLong(request.getParentId().toString()))
                .orElseThrow(() -> new IllegalArgumentException("Указаная несуществующая родительская возможность"));

        String businessKey = code + "_" + System.currentTimeMillis();


        OrderBusinessCapability orderBc = OrderBusinessCapability.builder()
                .id(orderBcRepository.findMaxId() + 1)
                .code(code)
                .name(request.getName())
                .description(request.getDescription())
                .owner(request.getOwner())
                .author(request.getAuthor())
                .status("PROPOSED")
                .isDomain(false)
                .parentId(request.getParentId())
                .mutableBcId(mutableBcId)
                .createdDate(LocalDateTime.now())
                .businessKey(null)
                .orderOwnerId(Integer.parseInt(RequestContext.getUserId()))
                .build();
        log.info("save bc");
        orderBc = orderBcRepository.save(orderBc);
        orderBcRepository.save(orderBc);
        return businessKey;
    }
}
