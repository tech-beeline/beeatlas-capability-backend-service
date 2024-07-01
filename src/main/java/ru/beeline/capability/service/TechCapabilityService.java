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
import ru.beeline.capability.dto.CapabilityParentDTO;
import ru.beeline.capability.dto.TechCapabilityDTO;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.exception.ValidationException;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.mapper.TechCapabilityMapper;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.TechCapabilityRelationsRepository;
import ru.beeline.capability.repository.TechCapabilityRepository;
import ru.beeline.fdmlib.dto.capability.PutTechCapabilityDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.beeline.capability.utils.Constants.CREATE;
import static ru.beeline.capability.utils.Constants.ENTITY_TYPE_TECH_CAPABILITY;
import static ru.beeline.capability.utils.Constants.UPDATE;

@Service
@Transactional
public class TechCapabilityService {

    @Autowired
    private TechCapabilityMapper techCapabilityMapper;

    @Autowired
    private BusinessCapabilityService businessCapabilityService;

    @Autowired
    private TechCapabilityRepository techCapabilityRepository;

    @Autowired
    private TechCapabilityRelationsRepository techCapabilityRelationsRepository;

    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;

    @Autowired
    private FindNameSortTableService findNameSortTableService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${queue.change-tech-capability.name}")
    private String changeTechCapabilityQueueName;

    public List<TechCapabilityDTO> getCapabilities(Integer limit, Integer offset) {
        if (offset == null) {
            offset = 0;
        }
        Pageable pageable = new OffsetBasedPageRequest(offset, limit == null || limit == 0 ? Integer.MAX_VALUE : limit, Sort.by(Sort.Direction.ASC, "name"));
        Page<TechCapability> techCapabilities = techCapabilityRepository.findCapabilities(pageable);
        return TechCapabilityDTO.convert(techCapabilities.toList());
    }

    public List<TechCapability> getByIdIn(List<Long> ids) {
        return techCapabilityRepository.findAllByIdIn(ids).stream()
                .filter(tech -> tech.getDeletedDate() == null)
                .collect(Collectors.toList());
    }

    public TechCapabilityDTO getCapabilityById(Long id) {
        TechCapability techCapability = techCapabilityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tech Capability не найдено"));
        techCapability.setParents(techCapability.getParents().stream()
                .filter(businessCapability -> Objects.isNull(businessCapability.getBusinessCapability().getDeletedDate()))
                .collect(Collectors.toList()));
        return TechCapabilityDTO.convert(techCapability);
    }

    public CapabilityParentDTO getParents(Long id) {
        ArrayList<CapabilityParentDTO> parents = new ArrayList<>();
        TechCapability techCapability = techCapabilityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tech Capability не найдено"));
        List<TechCapabilityRelations> techCapabilityRelations = techCapabilityRelationsRepository.findByTechCapability(techCapability);
        if (!techCapabilityRelations.isEmpty()) {
            parents.add(
                    new CapabilityParentDTO(
                            techCapabilityRelations.stream()
                                    .map(TechCapabilityRelations::getBusinessCapability)
                                    .map(BusinessCapability::getId)
                                    .collect(Collectors.toList())
                    )
            );
            techCapabilityRelations.forEach(relation -> {
                parents.add(businessCapabilityService.getParents(relation.getBusinessCapability().getId()));
            });
        }

        return mergeAndRemoveDuplicates(parents);
    }

    private CapabilityParentDTO mergeAndRemoveDuplicates(ArrayList<CapabilityParentDTO> parentsList) {
        Set<Long> uniqueParents = new HashSet<>();

        for (CapabilityParentDTO dto : parentsList) {
            uniqueParents.addAll(dto.getParents());
        }

        CapabilityParentDTO result = new CapabilityParentDTO();
        result.setParents(new ArrayList<>(uniqueParents));

        return result;
    }

    public void createOrUpdate(PutTechCapabilityDTO techCapability) {
        Optional<TechCapability> currentTechCapabilityOpt = techCapabilityRepository.findByCode(techCapability.getCode());
        boolean techCapabilityHaveParents = techCapability.getParents() != null && !techCapability.getParents().isEmpty();
        TechCapability currentTechCapability;
        if (!currentTechCapabilityOpt.isPresent()) {
            currentTechCapability = createTechCapability(techCapability);
            if (techCapabilityHaveParents) {
                createRelations(currentTechCapability, businessCapabilityRepository.findAllByCodeIn(techCapability.getParents()));
            }
            sendNotify(currentTechCapability.getId(), CREATE, changeTechCapabilityQueueName, techCapability.getName());
            findNameSortTableService.updateVector(currentTechCapability.getId(), currentTechCapability.getName(), currentTechCapability.getDescription(), currentTechCapability.getCode(), ENTITY_TYPE_TECH_CAPABILITY);
        } else {
            currentTechCapability = currentTechCapabilityOpt.get();
            PutTechCapabilityDTO currentTechCapabilityDTO = techCapabilityMapper.convertToPutTechCapabilityDTO(currentTechCapability);
            if (!techCapability.equals(currentTechCapabilityDTO)) {
                updateTechCapability(currentTechCapability, techCapability);
                findNameSortTableService.updateVector(currentTechCapability.getId(), currentTechCapability.getName(), currentTechCapability.getDescription(), currentTechCapability.getCode(), ENTITY_TYPE_TECH_CAPABILITY);
                if (techCapabilityHaveParents) {
                    Collections.sort(currentTechCapabilityDTO.getParents());
                    Collections.sort(techCapability.getParents());
                    if (!techCapability.equals(currentTechCapabilityDTO)) {
                        createRelations(currentTechCapability, businessCapabilityRepository.findAllByCodeIn(techCapability.getParents()));
                    }
                }
                sendNotify(currentTechCapability.getId(), UPDATE, changeTechCapabilityQueueName, techCapability.getName());
            }
        }

    }

    private void createRelations(TechCapability currentTechCapability, List<BusinessCapability> businessCapabilities) {
        List<TechCapabilityRelations> techCapabilityRelations = new ArrayList<>();
        for (BusinessCapability businessCapability : businessCapabilities) {
            TechCapabilityRelations techCapabilityRelation = new TechCapabilityRelations();
            techCapabilityRelation.setBusinessCapability(businessCapability);
            techCapabilityRelation.setTechCapability(currentTechCapability);
            techCapabilityRelations.add(techCapabilityRelation);
        }
        techCapabilityRelationsRepository.saveAll(techCapabilityRelations);
    }

    private void updateTechCapability(TechCapability currentTechCapability, PutTechCapabilityDTO techCapability) {
        currentTechCapability.setName(techCapability.getName());
        currentTechCapability.setDescription(techCapability.getDescription());
        currentTechCapability.setAuthor(techCapability.getAuthor());
        currentTechCapability.setOwner(techCapability.getOwner());
        currentTechCapability.setLastModifiedDate(new Date());
        currentTechCapability.setLink(techCapability.getLink());
        currentTechCapability.setStatus(techCapability.getStatus());
        techCapabilityRepository.save(currentTechCapability);
    }

    private TechCapability createTechCapability(PutTechCapabilityDTO techCapability) {
        TechCapability newTechCapability = TechCapability.builder()
                .code(techCapability.getCode())
                .name(techCapability.getName())
                .createdDate(new Date())
                .lastModifiedDate(new Date())
                .description(techCapability.getDescription())
                .author(techCapability.getAuthor())
                .owner(techCapability.getOwner())
                .link(techCapability.getLink())
                .status(techCapability.getStatus())
                .build();
        newTechCapability = techCapabilityRepository.save(newTechCapability);
        return newTechCapability;
    }

    private void sendNotify(Long id, String changeType, String queueName, String name) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            ObjectNode messagePayload = objectMapper.createObjectNode();
            messagePayload.put("entity_id", id);
            messagePayload.put("name", name);
            messagePayload.put("change_type", changeType);

            String message = objectMapper.writeValueAsString(messagePayload);

            sendMessageToTechCapabilityQueue(queueName, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void validateTechCapabilityDTO(PutTechCapabilityDTO techCapability) {
        StringBuilder errMsg = new StringBuilder();
        if (techCapability.getCode() == null) {
            errMsg.append("Отсутсвует обязательное поле code\n");
        }
        if (techCapability.getName() == null) {
            errMsg.append("Отсутсвует обязательное поле name\n");
        }
        if (techCapability.getAuthor() == null) {
            errMsg.append("Отсутсвует обязательное поле author\n");
        }
        if (!errMsg.toString().isEmpty()) {
            throw new ValidationException(errMsg.toString());
        }
    }


    public void sendMessageToTechCapabilityQueue(String queue, String message) {
        rabbitTemplate.convertAndSend(queue, message, messagePostProcessor -> {
            messagePostProcessor.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return messagePostProcessor;
        });
    }
}
