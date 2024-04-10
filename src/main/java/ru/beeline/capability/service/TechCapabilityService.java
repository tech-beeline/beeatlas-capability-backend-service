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
import ru.beeline.capability.dto.PutTechCapabilityDTO;
import ru.beeline.capability.dto.TechCapabilityDTO;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.repository.TechCapabilityRelationsRepository;
import ru.beeline.capability.repository.TechCapabilityRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void putCapability(PutTechCapabilityDTO capability) {
        Optional<TechCapability> techCapabilityOptional = techCapabilityRepository.findByCode(capability.getCode());
        if (techCapabilityOptional.isPresent()) {
            TechCapability techCapability = techCapabilityOptional.get();
            if (!capability.equals(techCapability)) {
                techCapability.setName(capability.getName());
                techCapability.setDescription(capability.getDescription());
                techCapability.setStatus(capability.getStatus());
                techCapability.setLastModifiedDate(new Date());
                techCapability.setAuthor(capability.getAuthor());
                techCapability.setLink(capability.getLink());
                techCapability.setOwner(capability.getOwner());
                TechCapability result = techCapabilityRepository.save(techCapability);
                if (!capability.getParents().isEmpty()) {
                    List<Long> childIds = capability.getParents().stream().map(Long::parseLong).collect(Collectors.toList());
                    List<BusinessCapability> businessCapabilities = businessCapabilityService.getByIdIn(childIds);
                    techCapabilityRelationsRepository.deleteAllByTechCapability(techCapability);
                    techCapabilityRelationsRepository.saveAll(businessCapabilities.stream().map(businessCapability -> TechCapabilityRelations.builder()
                            .businessCapability(businessCapability)
                            .techCapability(techCapability)
                            .build()).collect(Collectors.toList())
                    );
                }
                sendNotify(result.getId(), UPDATE, changeTechCapabilityQueueName);
            }
        } else {
            createCapabilities(capability);
        }
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

    private TechCapability createCapabilities(PutTechCapabilityDTO capability) {
        TechCapability result = techCapabilityRepository.save(TechCapability.builder()
                .code(capability.getCode())
                .name(capability.getName())
                .description(capability.getDescription())
                .status(capability.getStatus())
                .createdDate(new Date())
                .lastModifiedDate(new Date())
                .author(capability.getAuthor())
                .link(capability.getLink())
                .owner(capability.getOwner())
                .build()
        );
        if (!capability.getParents().isEmpty()) {
            List<Long> childIds = capability.getParents().stream().map(Long::parseLong).collect(Collectors.toList());
            List<BusinessCapability> businessCapabilities = businessCapabilityService.getByIdIn(childIds);
            techCapabilityRelationsRepository.deleteAllByTechCapability(result);
            techCapabilityRelationsRepository.saveAll(businessCapabilities.stream().map(businessCapability -> TechCapabilityRelations.builder()
                    .businessCapability(businessCapability)
                    .techCapability(result)
                    .build()).collect(Collectors.toList())
            );
        }

        sendNotify(result.getId(), CREATE, changeTechCapabilityQueueName);
        return result;
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
