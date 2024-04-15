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
import ru.beeline.capability.domain.*;
import ru.beeline.capability.dto.CapabilityParentDTO;
import org.springframework.transaction.annotation.Transactional;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;
import ru.beeline.capability.dto.PutTechCapabilityDTO;
import ru.beeline.capability.dto.TechCapabilityDTO;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.repository.*;

import java.util.stream.Collectors;

import static ru.beeline.capability.utils.Constants.ENTITY_TYPE_TECH_CAPABILITY;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static ru.beeline.capability.utils.Constants.CREATE;
import static ru.beeline.capability.utils.Constants.UPDATE;

@Service
@Transactional
public class TechCapabilityService {

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

    @Transactional
    public void createOrUpdate(PutTechCapabilityDTO techCapability) {
        if(techCapability.getParents() != null && !techCapability.getParents().isEmpty()) {
            List<BusinessCapability> businessCapabilities = businessCapabilityRepository.findAllByCodeIn(techCapability.getParents());
            Optional<TechCapability> currentTechCapabilityOpt = techCapabilityRepository.findByCode(techCapability.getCode());
            TechCapability currentTechCapability;
            if (!currentTechCapabilityOpt.isPresent()) {
                currentTechCapability = createTechCapability(techCapability);
                if(!businessCapabilities.isEmpty()) {
                    createRelations(currentTechCapability, businessCapabilities);
                }
                sendNotify(currentTechCapability.getId(), CREATE, changeTechCapabilityQueueName);
            } else {
                currentTechCapability = currentTechCapabilityOpt.get();
                updateTechCapability(currentTechCapability, techCapability);
                deleteAllRelationsByTCAndBC(currentTechCapability, businessCapabilities);
                if(!businessCapabilities.isEmpty()) {
                    createRelations(currentTechCapability, businessCapabilities);
                }
                sendNotify(currentTechCapability.getId(), UPDATE, changeTechCapabilityQueueName);
            }
            findNameSortTableService.updateVector(currentTechCapability.getId(), currentTechCapability.getName(), currentTechCapability.getDescription(), currentTechCapability.getCode(), ENTITY_TYPE_TECH_CAPABILITY);
        }
    }

    private void deleteAllRelationsByTCAndBC(TechCapability currentTechCapability, List<BusinessCapability> businessCapabilities) {
        for (BusinessCapability businessCapability : businessCapabilities) {
            techCapabilityRelationsRepository.deleteByBusinessCapabilityAndTechCapability(businessCapability, currentTechCapability);
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
        currentTechCapability.setLink(techCapability.getLink());
        currentTechCapability.setStatus(techCapability.getStatus());
        techCapabilityRepository.save(currentTechCapability);
    }

    private TechCapability createTechCapability(PutTechCapabilityDTO techCapability) {
        TechCapability newTechCapability = TechCapability.builder()
                .code(techCapability.getCode())
                .name(techCapability.getName())
                .description(techCapability.getDescription())
                .author(techCapability.getAuthor())
                .owner(techCapability.getOwner())
                .link(techCapability.getLink())
                .status(techCapability.getStatus())
                .build();
        newTechCapability = techCapabilityRepository.save(newTechCapability);
        return newTechCapability;
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
}
