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
import ru.beeline.capability.mapper.BusinessCapabilityMapper;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.TechCapabilityRelationsRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.beeline.capability.utils.Constants.CREATE;
import static ru.beeline.capability.utils.Constants.ENTITY_TYPE_BUSINESS_CAPABILITY;
import static ru.beeline.capability.utils.Constants.UPDATE;

@Service
@Transactional
public class BusinessCapabilityService {

    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;

    @Autowired
    private TechCapabilityRelationsRepository techCapabilityRelationsRepository;

    @Autowired
    private FindNameSortTableService findNameSortTableService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private BusinessCapabilityMapper businessCapabilityMapper;

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
        return businessCapabilityMapper.convert(techCapabilities, businessCapabilitiesKids);
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
        return businessCapabilityRepository.findAllByIdIn(ids).stream()
                .filter(bc -> bc.getDeletedDate() == null)
                .collect(Collectors.toList());
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

    public void putCapability(PutBusinessCapabilityDTO capabilityDTO) {
        Optional<BusinessCapability> businessCapabilityOptional = businessCapabilityRepository.findByCode(capabilityDTO.getCode());
        BusinessCapability businessCapability;
        if (businessCapabilityOptional.isPresent()) {
            businessCapability = businessCapabilityOptional.get();
            if (!capabilityDTO.equals(businessCapability)) {
                businessCapability = updateCapability(businessCapability, capabilityDTO);
                sendNotify(businessCapability.getId(), UPDATE, changeBusinessCapabilityQueueName);
            }
        } else {
            businessCapability = createCapabilities(capabilityDTO);
        }
        findNameSortTableService.updateVector(businessCapability.getId(), businessCapability.getName(), businessCapability.getDescription(), businessCapability.getCode(), ENTITY_TYPE_BUSINESS_CAPABILITY);
    }

    private BusinessCapability updateCapability(BusinessCapability businessCapability, PutBusinessCapabilityDTO capabilityDTO) {
        businessCapability.setName(capabilityDTO.getName());
        businessCapability.setDescription(capabilityDTO.getDescription());
        businessCapability.setStatus(capabilityDTO.getStatus());
        businessCapability.setAuthor(capabilityDTO.getAuthor());
        businessCapability.setCreatedDate(new Date());
        businessCapability.setLastModifiedDate(new Date());
        businessCapability.setLink(capabilityDTO.getLink());
        businessCapability.setOwner(capabilityDTO.getOwner());
        businessCapability.setParentId(Long.parseLong(capabilityDTO.getParent()));
        return businessCapabilityRepository.save(businessCapability);
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
