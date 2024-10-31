package ru.beeline.capability.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.beeline.capability.domain.*;
import ru.beeline.capability.dto.CapabilityParentDTO;
import ru.beeline.capability.dto.TechCapabilityDTO;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.exception.ValidationException;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.mapper.TechCapabilityMapper;
import ru.beeline.capability.repository.*;
import ru.beeline.capability.utils.UrlWrapper;
import ru.beeline.fdmlib.dto.capability.PutTechCapabilityDTO;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.beeline.capability.utils.Constants.*;

@Slf4j
@Service
@Transactional
public class TechCapabilityService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    EntityTypeRepository entityTypeRepository;

    @Autowired
    FindNameSortTableRepository findNameSortTableRepository;

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

    @Autowired
    private EnumCriteriaRepository enumCriteriaRepository;

    @Autowired
    private CriteriaBcRepository criteriaBcRepository;

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
        return techCapabilityRepository.findAllByIdIn(ids).stream().filter(tech -> tech.getDeletedDate() == null).collect(Collectors.toList());
    }

    public TechCapabilityDTO getCapabilityById(Long id) {
        TechCapability techCapability = techCapabilityRepository.findById(id).orElseThrow(() -> new NotFoundException("Tech Capability не найдено"));
        if (techCapability.getDeletedDate() != null) {
            throw new NotFoundException("Tech Capability не найдено");
        }
        techCapability.getParents().get(0);
        entityManager.detach(techCapability);
        techCapability.setParents(techCapability.getParents().stream().filter(businessCapability -> Objects.isNull(businessCapability.getBusinessCapability().getDeletedDate())).collect(Collectors.toList()));
        return TechCapabilityDTO.convert(techCapability);
    }

    public CapabilityParentDTO getParents(Long id) {
        ArrayList<CapabilityParentDTO> parents = new ArrayList<>();
        TechCapability techCapability = techCapabilityRepository.findById(id).orElseThrow(() -> new NotFoundException("Tech Capability не найдено"));
        List<TechCapabilityRelations> techCapabilityRelations = techCapabilityRelationsRepository.findByTechCapability(techCapability);
        if (!techCapabilityRelations.isEmpty()) {
            parents.add(new CapabilityParentDTO(techCapabilityRelations.stream().map(TechCapabilityRelations::getBusinessCapability).map(BusinessCapability::getId).collect(Collectors.toList())));
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
        log.info("techCapabilityHaveParents:" + techCapabilityHaveParents);
        TechCapability currentTechCapability;
        if (!currentTechCapabilityOpt.isPresent()) {
            log.info("techCapability find By Code: " + currentTechCapabilityOpt);
            log.info("currentTechCapabilityOpt isn't present");
            currentTechCapability = createTechCapability(techCapability);
            techCapabilityRelationsRepository.deleteAllByTechCapability(currentTechCapability);
            if (techCapabilityHaveParents) {
                log.info("create relations");
                createRelations(currentTechCapability, businessCapabilityRepository.findAllByCodeIn(techCapability.getParents()));
            }
            log.info("send notify");
            sendNotify(currentTechCapability.getId(), CREATE, changeTechCapabilityQueueName, techCapability.getName());
            findNameSortTableService.updateVector(currentTechCapability.getId(), currentTechCapability.getName(), currentTechCapability.getDescription(), currentTechCapability.getCode(), ENTITY_TYPE_TECH_CAPABILITY);
        } else {
            log.info("techCapability find By Code: " + currentTechCapabilityOpt);
            log.info("currentTechCapabilityOpt is present");
            currentTechCapability = currentTechCapabilityOpt.get();
            PutTechCapabilityDTO currentTechCapabilityDTO = techCapabilityMapper.convertToPutTechCapabilityDTO(currentTechCapability);
            log.info("check equals old techCapability and new techCapability");
            techCapability.setDescription(UrlWrapper.proxyUrl(techCapability.getDescription()));
            if (!techCapability.equals(currentTechCapabilityDTO)) {
                log.info("techCapability from BD find By Code: " + currentTechCapability);
                log.info("techCapability from dashboard: " + techCapability + " techCapabilityBD after Convert to PutCapability "
                        + currentTechCapabilityDTO);
                log.info("old techCapability and new techCapability is not equals, and try update");
                updateTechCapability(currentTechCapability, techCapability);
                log.info("delete old relations");
                techCapabilityRelationsRepository.deleteAllByTechCapability(currentTechCapability);
                findNameSortTableService.updateVector(currentTechCapability.getId(), currentTechCapability.getName(), currentTechCapability.getDescription(), currentTechCapability.getCode(), ENTITY_TYPE_TECH_CAPABILITY);
                if (techCapabilityHaveParents) {
                    Collections.sort(currentTechCapabilityDTO.getParents());
                    Collections.sort(techCapability.getParents());
                    log.info("create new relations");
                    createRelations(currentTechCapability, businessCapabilityRepository.findAllByCodeIn(techCapability.getParents()));
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
            log.info("check exist relations idBC=" + businessCapability.getId() + "and idTC=" + currentTechCapability.getId());
            if (!techCapabilityRelationsRepository.existsByBusinessCapabilityAndTechCapability(businessCapability, currentTechCapability)) {
                techCapabilityRelations.add(techCapabilityRelation);
            }
        }
        techCapabilityRelationsRepository.saveAll(techCapabilityRelations);
    }

    private void updateTechCapability(TechCapability currentTechCapability, PutTechCapabilityDTO techCapability) {
        currentTechCapability.setName(techCapability.getName());
        currentTechCapability.setDescription(UrlWrapper.proxyUrl(techCapability.getDescription()));
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
                .description(UrlWrapper.proxyUrl(techCapability.getDescription()))
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
            errMsg.append("Отсутствует обязательное поле code\n");
        }
        if (techCapability.getName() == null) {
            errMsg.append("Отсутствует обязательное поле name\n");
        }
        if (techCapability.getAuthor() == null) {
            errMsg.append("Отсутствует обязательное поле author\n");
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

    public void calculatePrivateTechCapabilitiesCount(Long entityId) {
        List<TechCapabilityRelations> techCapabilityRelationsList = techCapabilityRelationsRepository.findByTechCapability(
                techCapabilityRepository.findById(entityId).get());
        List<BusinessCapability> parentList = techCapabilityRelationsList.stream()
                .map(TechCapabilityRelations::getBusinessCapability)
                .collect(Collectors.toList());
        EnumCriteria quantityTc = enumCriteriaRepository.findByName("quantity_tc");
        parentList.forEach(businessCapability -> {
            Boolean newCriteria = false;
            CriteriasBc criteriasBc = criteriaBcRepository.findByBcIdAndCriterionId(businessCapability.getId(), quantityTc.getId());
            if (criteriasBc != null) {
                criteriasBc.setValue(criteriasBc.getValue() + 1);
                criteriaBcRepository.save(criteriasBc);
            } else {
                newCriteria = true;
                criteriasBc = criteriaBcRepository.save(CriteriasBc.builder()
                        .criterionId(quantityTc.getId())
                        .value(1)
                        .grade(2)
                        .bcId(businessCapability.getId())
                        .build());
            }
            List<BusinessCapability> businessCapabilityParentList =
                    businessCapabilityService.getBusinessCapabilityParentList(businessCapability.getId());
            businessCapabilityParentList.remove(0);
            CriteriasBc finalCriteriasBc = criteriasBc;
            Boolean finalNewCriteria = newCriteria;
            businessCapabilityParentList.forEach(bc -> iterateChildrenCriteriaBc(bc, quantityTc, finalCriteriasBc, finalNewCriteria, false));
        });
    }

    private void iterateChildrenCriteriaBc(BusinessCapability bc, EnumCriteria quantityTc, CriteriasBc criteriaParentBc, Boolean newCriteria, Boolean totalCount) {
        CriteriasBc criteriasBc = criteriaBcRepository.findByBcIdAndCriterionId(bc.getId(), quantityTc.getId());
        if (totalCount) {
            if (criteriasBc != null) {
                criteriasBc.setValue(criteriaParentBc.getValue());
                criteriasBc.setGrade(getGradeOfChild(bc, quantityTc));
                criteriaBcRepository.save(criteriasBc);
            } else {
                criteriaBcRepository.save(CriteriasBc.builder()
                        .criterionId(quantityTc.getId())
                        .value(criteriaParentBc.getValue())
                        .grade(getGrade(bc))
                        .bcId(bc.getId())
                        .build());
            }
        } else {
            if (criteriasBc != null) {
                criteriasBc.setValue(criteriasBc.getValue() + 1);
                if (newCriteria) {
                    criteriasBc.setGrade(getGradeOfChild(bc, quantityTc));
                }
                criteriaBcRepository.save(criteriasBc);
            } else {
                criteriaBcRepository.save(CriteriasBc.builder()
                        .criterionId(quantityTc.getId())
                        .value(1)
                        .grade(getGrade(bc))
                        .bcId(bc.getId())
                        .build());
            }
        }
    }

    private int getGradeOfChild(BusinessCapability bc, EnumCriteria quantityTc) {
        AtomicInteger criteriaIterator = new AtomicInteger();
        AtomicInteger valueSummary = new AtomicInteger();
        businessCapabilityService.getChildrenBC(bc).forEach(childChildBc -> {
            CriteriasBc childCriteriasBc = criteriaBcRepository.findByBcIdAndCriterionId(childChildBc.getId(), quantityTc.getId());
            if (childCriteriasBc != null) {
                if (childCriteriasBc.getGrade() == 2) {
                    criteriaIterator.getAndIncrement();
                }
                valueSummary.addAndGet(childCriteriasBc.getValue());
            }
        });
        if (businessCapabilityService.getChildrenBC(bc).size() == criteriaIterator.get()) {
            return 2;

        } else {
            return 1;
        }
    }

    private int getGrade(BusinessCapability bc) {
        if (businessCapabilityService.getChildrenBC(bc).size() > 1) {
            return 1;
        } else {
            return 2;
        }
    }

    public void calculateTotalTechCapabilitiesCount() {
        List<TechCapabilityRelations> techCapabilityRelationsList = techCapabilityRelationsRepository.findAll();
        List<BusinessCapability> parentList = techCapabilityRelationsList.stream()
                .map(TechCapabilityRelations::getBusinessCapability)
                .distinct()
                .collect(Collectors.toList());
        EnumCriteria quantityTc = enumCriteriaRepository.findByName("quantity_tc");
        parentList.forEach(businessCapability -> {
            Boolean newCriteria = false;
            CriteriasBc criteriasBc = criteriaBcRepository.findByBcIdAndCriterionId(businessCapability.getId(), quantityTc.getId());
            if (criteriasBc != null) {
                criteriasBc.setValue(businessCapabilityRepository.findAllByParentId(businessCapability.getId()).size());
                criteriaBcRepository.save(criteriasBc);
            } else {
                newCriteria = true;
                criteriasBc = criteriaBcRepository.save(CriteriasBc.builder()
                        .criterionId(quantityTc.getId())
                        .value(techCapabilityRelationsRepository.findByBusinessCapability(businessCapability).size())
                        .grade(2)
                        .bcId(businessCapability.getId())
                        .build());
            }
            List<BusinessCapability> businessCapabilityParentList =
                    businessCapabilityService.getBusinessCapabilityParentList(businessCapability.getId());
            businessCapabilityParentList.remove(0);
            CriteriasBc finalCriteriasBc = criteriasBc;
            Boolean finalNewCriteria = newCriteria;
            businessCapabilityParentList.forEach(bc -> iterateChildrenCriteriaBc(bc, quantityTc, finalCriteriasBc, finalNewCriteria, true));
        });
    }

    public void deleteTechCapability(String code) {
        Optional<TechCapability> optionalTechCapability = techCapabilityRepository.findByCode(code);
        if (optionalTechCapability.isPresent()) {
            if (optionalTechCapability.get().getDeletedDate() == null) {
                Long techCapabilityId = optionalTechCapability.get().getId();
                optionalTechCapability.map(techCapability -> {
                    techCapability.setDeletedDate(new Date());
                    return techCapabilityRepository.save(techCapability);
                });
                EntityType entityType = entityTypeRepository.findByName("TECH_CAPABILITY");
                findNameSortTableRepository.deleteByRefIdAndType(techCapabilityId, entityType);
            }
        }
    }
}
