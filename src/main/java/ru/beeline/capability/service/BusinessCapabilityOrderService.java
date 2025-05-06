package ru.beeline.capability.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.beeline.capability.client.BpmClient;
import ru.beeline.capability.controller.RequestContext;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.OrderBusinessCapability;
import ru.beeline.capability.dto.BusinessCapabilityOrderPatchRequestDTO;
import ru.beeline.capability.dto.BusinessCapabilityOrderRequestDTO;
import ru.beeline.capability.exception.ValidationException;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.OrderBusinessCapabilityRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class BusinessCapabilityOrderService {

    @Autowired
    private BusinessCapabilityRepository bcRepository;
    @Autowired
    private OrderBusinessCapabilityRepository orderBcRepository;
    @Autowired
    private BpmClient bpmClient;
    @Autowired
    private OrderBusinessCapabilityRepository orderBusinessCapabilityRepository;

    public void editOrder(Integer id, BusinessCapabilityOrderPatchRequestDTO request, String statusAlias) {
        OrderBusinessCapability orderBusinessCapability = orderBcRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OrderBusinessCapability не найдена"));
        BusinessCapability parentBusinessCapability = bcRepository.findById(Long.parseLong(id.toString()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Родительская BC не найдена или не является доменной"));
        orderBusinessCapability.setName(request.getName());
        orderBusinessCapability.setDescription(request.getDescription());
        orderBusinessCapability.setOwner(request.getOwner());
        orderBusinessCapability.setParentId(request.getParentId());
        orderBusinessCapability.setLastModifiedDate(LocalDateTime.now());
        orderBusinessCapabilityRepository.save(orderBusinessCapability);
        if (statusAlias != null){
            bpmClient.editStatusProcess(request.getComment(), orderBusinessCapability.getBusinessKey(), statusAlias);
        }
    }

    public String createOrder(BusinessCapabilityOrderRequestDTO request) {
        validateRequest(request);
        Long mutableBcId = request.getMutableBcId();

        String code;
        if (mutableBcId != null) {
            BusinessCapability mutableBc = bcRepository.findById(mutableBcId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "изменение не существующей BC"));
            code = mutableBc.getCode();
        } else {
            Integer maxId = orderBcRepository.findMaxId();
            long nextId = maxId + 1;
            code = String.format("NEW.BC-%06d", nextId);
        }

        bcRepository.findByIdAndDeletedDateIsNullAndIsDomainTrue(request.getParentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Родительская BC не найдена или не является доменной"));

        String businessKey = code + "_" + System.currentTimeMillis();


        OrderBusinessCapability orderBc = OrderBusinessCapability.builder()
                .id(orderBcRepository.findMaxId() + 1)
                .code(code)
                .name(request.getName())
                .description(request.getDescription())
                .owner(request.getOwner())
                .author(request.getAuthor())
                .status("PROPOSED")
                .parentId(request.getParentId())
                .mutableBcId(mutableBcId)
                .createdDate(LocalDateTime.now())
                .businessKey(businessKey)
                .build();

        orderBcRepository.save(orderBc);

        Map<String, Object> variables = new HashMap<>();
        variables.put("authorId", RequestContext.getUserId());
        variables.put("type", mutableBcId == null ? "create_business_capability" : "update_business_capability");
        variables.put("comment", request.getComment() != null ? request.getComment() : "");
        variables.put("entityId", orderBc.getId().intValue());
        variables.put("name", request.getName());

        bpmClient.startProcess("your_process_key", businessKey, variables);

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
}
