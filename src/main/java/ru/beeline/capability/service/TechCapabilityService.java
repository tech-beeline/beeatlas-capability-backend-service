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
import ru.beeline.capability.dto.GetHistoryByIdDTO;
import ru.beeline.capability.dto.GetTcHistoryVersionDTO;
import ru.beeline.capability.dto.HistoryTechCapabilityDTO;
import ru.beeline.capability.dto.ParentDTO;
import ru.beeline.capability.dto.TechCapabilityDTO;
import ru.beeline.capability.dto.VersionInfoDTO;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.exception.ValidationException;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.mapper.TechCapabilityMapper;
import ru.beeline.capability.repository.*;
import ru.beeline.capability.utils.Node;
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

    @Autowired
    private HistoryTechCapabilityRepository historyTechCapabilityRepository;

    @Autowired
    private HistoryTechCapabilityRelationsRepository historyTechCapabilityRelationsRepository;

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
        return techCapabilityRepository.findAllByIdInAndDeletedDateIsNull(ids);
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
            log.info("currentTechCapabilityOpt is present");
            currentTechCapability = currentTechCapabilityOpt.get();
            PutTechCapabilityDTO currentTechCapabilityDTO = techCapabilityMapper.convertToPutTechCapabilityDTO(currentTechCapability);
            log.info("check equals old techCapability and new techCapability");
            if (equalsDashboardDTO(techCapability, currentTechCapabilityDTO)) {
                log.info("techCapability from dashboard: " + techCapability + " equals techCapability from BD "
                        + currentTechCapabilityDTO);
                log.info("old techCapability and new techCapability is not equals, and try update");
                addToHistory(currentTechCapability);
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

    private void addToHistory(TechCapability currentTechCapability) {
        Optional<HistoryTechCapability> historyTechCapability = historyTechCapabilityRepository.findTopByIdRefOrderByVersionDesc(currentTechCapability.getId());
        HistoryTechCapability newHistoryTechCapability = historyTechCapabilityRepository.save(HistoryTechCapability.builder()
                .idRef(currentTechCapability.getId())
                .version(historyTechCapability.isPresent() ? historyTechCapability.get().getVersion() + 1 : 1)
                .code(currentTechCapability.getCode())
                .name(currentTechCapability.getName())
                .description(currentTechCapability.getDescription())
                .modifiedDate(new Date())
                .owner(currentTechCapability.getOwner())
                .status(currentTechCapability.getStatus())
                .link(currentTechCapability.getLink())
                .author(currentTechCapability.getAuthor())
                .build());
        List<TechCapabilityRelations> relations = techCapabilityRelationsRepository.findByTechCapability(currentTechCapability);
        if (!relations.isEmpty()) {
            List<HistoryTechCapabilityRelations> forSave = relations.stream()
                    .map(rel -> HistoryTechCapabilityRelations.builder()
                            .idParent(rel.getBusinessCapability().getId())
                            .idHistoryChild(newHistoryTechCapability.getId())
                            .build())
                    .collect(Collectors.toList());
            historyTechCapabilityRelationsRepository.saveAll(forSave);
        }
    }

    private Boolean equalsDashboardDTO(PutTechCapabilityDTO techCapability, PutTechCapabilityDTO currentTechCapabilityDTO) {
        techCapability.setDescription(UrlWrapper.proxyUrl(techCapability.getDescription()));
        if (techCapability.getParents() != null) {
            Set<String> techCapabilityList = new TreeSet<>(techCapability.getParents());
            techCapability.setParents(new ArrayList<>(techCapabilityList));
        } else {
            techCapability.setParents(new ArrayList<>());
        }
        if (currentTechCapabilityDTO.getParents() != null) {
            Set<String> currentTechCapabilityDTOList = new TreeSet<>(currentTechCapabilityDTO.getParents());
            currentTechCapabilityDTO.setParents(new ArrayList<>(currentTechCapabilityDTOList));
        } else {
            currentTechCapabilityDTO.setParents(new ArrayList<>());
        }
        return !techCapability.equals(currentTechCapabilityDTO);


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
        currentTechCapability.setAuthor(techCapability.getAuthor() == null || techCapability.getAuthor().isEmpty() ?
                "Sparx EA" : techCapability.getAuthor());
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
                .author(techCapability.getAuthor() == null || techCapability.getAuthor().isEmpty() ?
                        "Sparx EA" : techCapability.getAuthor())
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

    public void calculateTotalTechCapabilitiesCount() {
        List<TechCapabilityRelations> techCapabilityRelationsList = techCapabilityRelationsRepository.findAll();
        Map<Long, Node> nodeMap = getNodeMap(techCapabilityRelationsList);
        EnumCriteria quantityTc = enumCriteriaRepository.findByName("quantity_tc");
        Map<Long, CriteriasBc> criteriasBcMap = new HashMap<>();
        Node parentNode = findRootNode(nodeMap, nodeMap.values().stream().findFirst().get().getId());
        calculateTree(parentNode, criteriasBcMap, quantityTc);
        criteriaBcRepository.deleteAllByCriterionId(quantityTc.getId());
        criteriaBcRepository.saveAll(criteriasBcMap.values().stream()
                .filter(criteriaBc -> criteriaBc.getValue() > 0)
                .collect(Collectors.toList()));
    }

    private void calculateTree(Node node, Map<Long, CriteriasBc> criteriasBcMap, EnumCriteria quantityTc) {
        if (node.getChildren().size() > 0) {
            AtomicInteger valueKidsSum = new AtomicInteger();
            node.getChildren().forEach(child -> {
                calculateTree(child, criteriasBcMap, quantityTc);
                valueKidsSum.addAndGet(Math.toIntExact(child.getValue()));
            });
            node.setValue(node.getCountTech() + valueKidsSum.get());
            node.setGrade(getGrade(node));
            CriteriasBc criteriasBc = CriteriasBc.builder()
                    .criterionId(quantityTc.getId())
                    .value(node.getValue().intValue())
                    .grade(node.getGrade().intValue())
                    .bcId(node.getId())
                    .build();
            criteriasBcMap.put(node.getId(), criteriasBc);
        } else {
            if (node.getCountTech().intValue() > 0) {
                CriteriasBc criteriasBc = CriteriasBc.builder()
                        .criterionId(quantityTc.getId())
                        .value(node.getCountTech().intValue())
                        .grade(2)
                        .bcId(node.getId())
                        .build();
                criteriasBcMap.put(node.getId(), criteriasBc);
                node.setValue(node.getCountTech());
                node.setGrade(2L);
            }
        }
    }

    private Long getGrade(Node node) {
        if ((node.getChildren().size() == 0 && node.getCountTech() > 0)
                || node.getChildren().size() == node.getChildren().stream().filter(child -> child.getGrade() == 2L).count()
                || node.getChildren().size() == node.getChildren().stream().filter(child -> child.getCountTech() > 0).count()
        ) {
            return 2L;
        }
        return 1L;
    }

    public Node findRootNode(Map<Long, Node> nodeMap, Long id) {
        return nodeMap.get(id).getParentId() != null ? findRootNode(nodeMap, nodeMap.get(id).getParentId()) : nodeMap.get(id);
    }

    private Map<Long, Node> getNodeMap(List<TechCapabilityRelations> techCapabilityRelationsList) {
        Map<Long, Node> nodeMap = new HashMap<>();
        for (BusinessCapability obj : businessCapabilityRepository.findAll()) {
            if (obj.getDeletedDate() == null) {
                Node node = new Node(obj.getId(), obj.getParentId());
                nodeMap.put(obj.getId(), node);
            }
        }
        for (Node node : nodeMap.values()) {
            if (node.getParentId() != null) {
                Node parent = nodeMap.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }
        techCapabilityRelationsList.forEach(relation -> {
            if (relation.getBusinessCapability().getDeletedDate() == null) {
                nodeMap.get(relation.getBusinessCapability().getId()).setCountTech(nodeMap.get(relation.getBusinessCapability().getId()).getCountTech() + 1);
            }
        });

        return nodeMap;
    }

    public void deleteTechCapability(String code) {
        Optional<TechCapability> optionalTechCapability = techCapabilityRepository.findByCode(code);
        if (optionalTechCapability.isPresent()) {
            TechCapability techCapability = optionalTechCapability.get();
            if (techCapability.getDeletedDate() == null) {
                Long techCapabilityId = techCapability.getId();
                techCapability.setDeletedDate(new Date());
                techCapabilityRepository.save(techCapability);
                EntityType entityType = entityTypeRepository.findByName("TECH_CAPABILITY");
                findNameSortTableRepository.deleteByRefIdAndType(techCapabilityId, entityType);
                techCapabilityRelationsRepository.deleteAllByTechCapability(techCapability);
            }
        }
    }

    public List<GetHistoryByIdDTO> getTechCapabilityHistory(Long id) {
        Optional<TechCapability> optionalTechCapability = techCapabilityRepository.findById(id);
        if (optionalTechCapability.isEmpty()) {
            throw new NotFoundException("Tech Capability не найдено");
        }
        TechCapability techCapability = optionalTechCapability.get();
        List<HistoryTechCapability> historyTCList = historyTechCapabilityRepository.findByIdRef(id);
        VersionInfoDTO versionInfo = VersionInfoDTO.builder()
                .version(1)
                .modified_date(techCapability.getLastModifiedDate())
                .author(techCapability.getAuthor())
                .build();
        if (historyTCList.isEmpty()) {
            return List.of(GetHistoryByIdDTO.builder()
                    .versionInfo(versionInfo)
                    .build());
        } else {
            List<VersionInfoDTO> versionInfoList = historyTCList.stream()
                    .map(historyTc -> VersionInfoDTO.builder()
                            .version(historyTc.getVersion().intValue())
                            .modified_date(historyTc.getModifiedDate())
                            .author(historyTc.getAuthor())
                            .build())
                    .collect(Collectors.toList());
            Integer lostVersion = versionInfoList.stream()
                    .mapToInt(VersionInfoDTO::getVersion)
                    .max()
                    .getAsInt();
            versionInfo.setVersion(lostVersion + 1);
            versionInfoList.add(versionInfo);
            versionInfoList.sort(Comparator.comparingInt(VersionInfoDTO::getVersion).reversed());

            return versionInfoList.stream()
                    .map(versionInfoDTO -> GetHistoryByIdDTO.builder()
                            .versionInfo(versionInfoDTO)
                            .build())
                    .collect(Collectors.toList());
        }
    }

    public List<GetTcHistoryVersionDTO> getTechCapabilityHistoryVersion(Long id, Integer version, Integer otherVersion) {
        TechCapability techCapability = techCapabilityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("TechCapability с id: %s не найдено", id)));
        HistoryTechCapability historyTcFirstVersion = findHistoryTcVersion(id, version);
        List<HistoryTechCapabilityDTO> result = new ArrayList<>();
        result.add(buildTcHistoryVersionDTO(historyTcFirstVersion, id, version));
        if (otherVersion != null) {
            HistoryTechCapability historyTcSecondVersion = findHistoryTcVersion(id, otherVersion);
            result.add(buildTcHistoryVersionDTO(historyTcSecondVersion, id, otherVersion));
        } else {
            Optional<HistoryTechCapability> optionalFindHistoryTcOtherVersion =
                    historyTechCapabilityRepository.findByIdRefOtherVersion(id);
            if (optionalFindHistoryTcOtherVersion.isPresent()) {
                List<TechCapabilityRelations> techCapabilityRelations = techCapabilityRelationsRepository
                        .findByTechCapability(techCapability);
                if (techCapabilityRelations.isEmpty()) {
                    throw new NotFoundException(String.format("Не найдено родительских BC для TC с Id: %s ",
                            techCapability.getId()));
                }
                List<ParentDTO> parentDTOS = new ArrayList<>();
                for (TechCapabilityRelations techCapabilityRelation : techCapabilityRelations) {
                    ParentDTO parentDTO = ParentDTO.builder()
                            .id(techCapabilityRelation.getId())
                            .code(techCapabilityRelation.getBusinessCapability().getCode())
                            .name(techCapabilityRelation.getBusinessCapability().getName())
                            .build();
                    parentDTOS.add(parentDTO);
                }
                parentDTOS.sort(Comparator.comparingLong(ParentDTO::getId));
                HistoryTechCapabilityDTO gethistoryTechCapabilityDTO = techCapabilityMapper.toHistoryTechCapabilityDTO(
                        optionalFindHistoryTcOtherVersion.get(), parentDTOS, id,
                        optionalFindHistoryTcOtherVersion.get().getVersion().intValue() + 1);
                result.add(gethistoryTechCapabilityDTO);
            } else {
                throw new NotFoundException("History Business Capability с последней версией не найдено");
            }
        }
        result.sort(Comparator.comparingInt(HistoryTechCapabilityDTO::getVersion).reversed());
        return result.stream()
                .map(capability -> GetTcHistoryVersionDTO.builder()
                        .techCapability(capability)
                        .build())
                .collect(Collectors.toList());
    }

    private HistoryTechCapability findHistoryTcVersion(Long id, Integer version) {
        return historyTechCapabilityRepository.findByIdRefAndVersion(id, version)
                .orElseThrow(() -> new NotFoundException(String.format("History Tech Capability с id: %d, version: %s не найдено", id, version)));
    }

    private HistoryTechCapabilityDTO buildTcHistoryVersionDTO(HistoryTechCapability historyTechCapability,
                                                              Long id, Integer version) {
        List<HistoryTechCapabilityRelations> result = historyTechCapabilityRelationsRepository
                .findAllByIdHistoryChild(historyTechCapability.getId());
        List<ParentDTO> parentDTOS = new ArrayList<>();
        if (result.isEmpty()) {
            throw new NotFoundException(String.format("Не найдено родительских Business Capability с Id: %s",
                    historyTechCapability.getId()));
        }
        for (HistoryTechCapabilityRelations historyTechCapabilityRelations : result) {
            Optional<BusinessCapability> optionalBusinessCapability = businessCapabilityRepository
                    .findById(historyTechCapabilityRelations.getIdParent());
            if (optionalBusinessCapability.isPresent()) {
                BusinessCapability parenBc = optionalBusinessCapability.get();
                ParentDTO parentDTO = ParentDTO.builder()
                        .id(historyTechCapabilityRelations.getIdParent())
                        .code(parenBc.getCode())
                        .name((parenBc.getName()))
                        .build();
                parentDTOS.add(parentDTO);
            }
        }
        parentDTOS.sort(Comparator.comparingLong(ParentDTO::getId));
        return techCapabilityMapper.toHistoryTechCapabilityDTO(historyTechCapability, parentDTOS, id, version);
    }
}
