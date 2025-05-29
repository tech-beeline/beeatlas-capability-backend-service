package ru.beeline.capability.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.beeline.capability.client.BpmClient;
import ru.beeline.capability.client.UserClient;
import ru.beeline.capability.controller.RequestContext;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.OrderBusinessCapability;
import ru.beeline.capability.dto.BusinessCapabilityOrderDraftRequestDTO;
import ru.beeline.capability.dto.BusinessCapabilityOrderDraftResponseDTO;
import ru.beeline.capability.dto.BusinessCapabilityOrderPatchRequestDTO;
import ru.beeline.capability.dto.BusinessCapabilityOrderRequestDTO;
import ru.beeline.capability.exception.ForbiddenException;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.exception.ValidationException;
import ru.beeline.capability.mapper.BusinessCapabilityOrderMapper;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.OrderBusinessCapabilityRepository;
import ru.beeline.fdmlib.dto.bpm.ApplicationExtendedDTO;

import java.time.LocalDateTime;
import java.util.*;
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
    @Autowired
    private UserClient userClient;

    public BusinessCapabilityOrderDraftResponseDTO getBusinessCapabilityOrderById(Integer id) {
        OrderBusinessCapability order = orderBcRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Сущность не найдена"));
        return BusinessCapabilityOrderMapper.getBusinessCapabilityOrderDraftResponseDTO(order);
    }

    public List<BusinessCapabilityOrderDraftResponseDTO> getBusinessCapabilityDraft() {
        List<OrderBusinessCapability> orderBusinessCapabilities = orderBcRepository.findByOrderOwnerIdAndBusinessKeyIsNull(
                Integer.parseInt(RequestContext.getUserId()));
        return orderBusinessCapabilities.stream()
                .sorted(Comparator.comparing(OrderBusinessCapability::getCreatedDate).reversed())
                .map(BusinessCapabilityOrderMapper::getBusinessCapabilityOrderDraftResponseDTO)
                .collect(Collectors.toList());
    }

    public void editOrderDraft(Integer id, BusinessCapabilityOrderRequestDTO request, Boolean publish) {
        OrderBusinessCapability orderBusinessCapability = orderBcRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("OrderBusinessCapability не найдена"));

        if (orderBusinessCapability.getOrderOwnerId() != Integer.parseInt(RequestContext.getUserId())) {
            throw new ForbiddenException("403 Forbidden");
        }
        if (orderBusinessCapability.getBusinessKey() != null) {
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
            code = String.format("NEW.BC-%06d", id);
        }
        boolean isUpdated = false;
        if (request.getName() != null && !request.getName().isEmpty() && !request.getName().equals(orderBusinessCapability.getName())) {
            orderBusinessCapability.setName(request.getName());
            isUpdated = true;
        }

        if (request.getDescription() != null && !request.getDescription().isEmpty() && !request.getDescription().equals(orderBusinessCapability.getDescription())) {
            orderBusinessCapability.setDescription(request.getDescription());
            isUpdated = true;
        }
        if (request.getOwner() != null && !request.getOwner().isEmpty() && !request.getOwner().equals(orderBusinessCapability.getOwner())) {
            orderBusinessCapability.setOwner(request.getOwner());
            isUpdated = true;
        }
        if (request.getParentId() != null && !request.getParentId().equals(orderBusinessCapability.getParentId())) {
            orderBusinessCapability.setParentId(request.getParentId());
            isUpdated = true;
            if(request.getParentId()!= null) {
                bcRepository.findByIdAndDeletedDateIsNull(Long.parseLong(request.getParentId().toString()))
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Указана несуществующая родительская возможность"));
            }
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

    @Transactional
    public void editOrder(Integer id, BusinessCapabilityOrderPatchRequestDTO request, String statusAlias) {
        OrderBusinessCapability orderBusinessCapability = orderBcRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("OrderBusinessCapability не найдена"));
        if (request.getParentId() != null) {
            bcRepository.findById(Long.parseLong(request.getParentId().toString()))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Родительская BC не найдена или не является доменной"));
        }
        ApplicationExtendedDTO app =bpmClient.getApplication(orderBusinessCapability.getBusinessKey());
        if (Integer.parseInt(RequestContext.getUserId())!=app.getAuthor().getId() &&
                Integer.parseInt(RequestContext.getUserId())!=app.getExecutor().getId()
        ) {
            throw new ForbiddenException("403 Forbidden");
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
            Integer maxId = orderBcRepository.getLastSequenceValue();
            long nextId = maxId + 1;
            code = String.format("NEW.BC-%06d", nextId);
        }

        log.info("search bc");
        bcRepository.findByIdAndDeletedDateIsNull(Long.parseLong(request.getParentId().toString()))
                .orElseThrow(() -> new IllegalArgumentException("Указана несуществующая родительская возможность"));

        String businessKey = code + "_" + System.currentTimeMillis();


        OrderBusinessCapability orderBc = OrderBusinessCapability.builder()
                .id(orderBcRepository.getLastSequenceValue() + 1)
                .code(code)
                .name(request.getName())
                .description(request.getDescription())
                .owner(request.getOwner())
                .author(userClient.getUserProfile(Integer.parseInt(RequestContext.getUserId())).getFullName())
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
            Integer maxId = orderBcRepository.getLastSequenceValue();
            long nextId = maxId + 1;
            code = String.format("NEW.BC-%06d", nextId);
        }

        if (request.getParentId() != null) {
            log.info("search bc");
            bcRepository.findByIdAndDeletedDateIsNull(Long.parseLong(request.getParentId().toString()))
                    .orElseThrow(() -> new IllegalArgumentException("Указана несуществующая родительская возможность"));
        }
        String businessKey = code + "_" + System.currentTimeMillis();


        OrderBusinessCapability orderBc = OrderBusinessCapability.builder()
                .id(orderBcRepository.getLastSequenceValue() + 1)
                .code(code)
                .name(request.getName())
                .description(request.getDescription())
                .owner(request.getOwner())
                .author(userClient.getUserProfile(Integer.parseInt(RequestContext.getUserId())).getFullName())
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
