package ru.beeline.capability.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;
import ru.beeline.capability.dto.BusinessCapabilityChildrenDTO;
import ru.beeline.capability.dto.BusinessCapabilityShortDTO;
import ru.beeline.capability.dto.CapabilityParentDTO;
import ru.beeline.capability.dto.PutBusinessCapabilityDTO;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.TechCapabilityRelationsRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.beeline.capability.utils.Constants.CREATE;
import static ru.beeline.capability.utils.Constants.UPDATE;

@Service
@Transactional
public class BusinessCapabilityService {

    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;

    @Autowired
    private TechCapabilityRelationsRepository techCapabilityRelationsRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${queue.change-business-capability.name}")
    private String changeBusinessCapabilityQueueName;

    public BusinessCapabilityChildrenDTO getChildren(Long id) {
        List<TechCapability> techCapabilities = businessCapabilityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Business Capability не найдено"))
                .getChildren().stream()
                .map(TechCapabilityRelations::getTechCapability)
                .filter(techCapability -> Objects.isNull(techCapability.getDeletedDate()))
                .collect(Collectors.toList());
        List<BusinessCapability> businessCapabilitiesKids = businessCapabilityRepository.findAllByParentIdAndDeletedDateIsNull(id);
        return BusinessCapabilityChildrenDTO.convert(techCapabilities, businessCapabilitiesKids);
    }

    public CapabilityParentDTO getParents(Long id) {
        ArrayList<Long> result = new ArrayList<>();
        while (true) {
            id = businessCapabilityRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Business Capability не найдено"))
                    .getParentId();

            if (Objects.isNull(id)) {
                return new CapabilityParentDTO(result);
            } else {
                result.add(id);
            }
        }
    }

    public BusinessCapabilityShortDTO getById(Long id) {
        BusinessCapability businessCapability = businessCapabilityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Business Capability не найдено"));

        if (businessCapability.getParentEntity() != null && businessCapability.getParentEntity().getDeletedDate() != null)
            businessCapability.setParentEntity(null);

        return BusinessCapabilityShortDTO.convert(businessCapability, checkHasKids(businessCapability));
    }

    public List<BusinessCapability> getByIdIn(List<Long> ids) {
        return businessCapabilityRepository.findAllByIdIn(ids);
    }

    private boolean checkHasKids(BusinessCapability businessCapability) {
        return techCapabilityRelationsRepository.existsByBusinessCapability(businessCapability) || businessCapabilityRepository.existsByParentId(businessCapability.getId());
    }

    public List<BusinessCapabilityShortDTO> getCapabilities(Integer limit, Integer offset, String findBy) {
        if (offset == null) {
            offset = 0;
        }
        Pageable pageable = new OffsetBasedPageRequest(offset, limit == null || limit == 0 ? Integer.MAX_VALUE : limit, Sort.by(Sort.Direction.ASC, "name"));
        Page<BusinessCapability> businessCapabilities = null;
        switch (FindBy.valueOf(findBy)) {
            case ALL:
                businessCapabilities = businessCapabilityRepository.findCapabilities(pageable);
                businessCapabilities.stream()
                        .filter(сapability -> Objects.nonNull(сapability.getParentEntity()) && Objects.nonNull(сapability.getParentEntity().getDeletedDate()))
                        .forEach(сapability -> сapability.setParentEntity(null));
                break;
            case CORE:
                businessCapabilities = businessCapabilityRepository.findCapabilitiesWithoutParent(pageable);
                break;
            default:
                throw new IllegalArgumentException("Invalid value for findBy: " + findBy);
        }

        return BusinessCapabilityShortDTO.convert(businessCapabilities.toList());
    }

    public void putCapability(PutBusinessCapabilityDTO capability) {
        Optional<BusinessCapability> businessCapabilityOptional = businessCapabilityRepository.findByCode(capability.getCode());
        if (businessCapabilityOptional.isPresent()) {
            BusinessCapability businessCapability = businessCapabilityOptional.get();
            if (!capability.equals(businessCapability)) {
                businessCapability.setName(capability.getName());
                businessCapability.setDescription(capability.getDescription());
                businessCapability.setStatus(capability.getStatus());
                businessCapability.setAuthor(capability.getAuthor());
                businessCapability.setCreatedDate(new Date());
                businessCapability.setLastModifiedDate(new Date());
                businessCapability.setLink(capability.getLink());
                businessCapability.setOwner(capability.getOwner());
                businessCapability.setParentId(Long.parseLong(capability.getParent()));
                BusinessCapability result = businessCapabilityRepository.save(businessCapability);
                sendNotify(result.getId(), UPDATE, changeBusinessCapabilityQueueName);
            }
        } else {
            createCapabilities(capability);
        }
    }

    private BusinessCapability createCapabilities(PutBusinessCapabilityDTO capability) {

        BusinessCapability result = businessCapabilityRepository.save(BusinessCapability.builder()
                .code(capability.getCode())
                .name(capability.getName())
                .description(capability.getDescription())
                .status(capability.getStatus())
                .author(capability.getAuthor())
                .createdDate(new Date())
                .lastModifiedDate(new Date())
                .link(capability.getLink())
                .owner(capability.getOwner())
                .parentId(Long.parseLong(capability.getParent()))
                .build()
        );
        sendNotify(result.getId(), CREATE, changeBusinessCapabilityQueueName);
        return result;
    }

    private void sendNotify(Long id, String changeType, String queueName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            ObjectNode messagePayload = objectMapper.createObjectNode();
            messagePayload.put("entity_id", id);
            messagePayload.put("change_type", changeType);

            String message = objectMapper.writeValueAsString(messagePayload);

            sendMessageToTechCapabilityQueue(queueName, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessageToTechCapabilityQueue(String queue, String message) {
        rabbitTemplate.convertAndSend(queue, message, messagePostProcessor -> {
            messagePostProcessor.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return messagePostProcessor;
        });
    }

    enum FindBy {
        ALL,
        CORE
    }

}
