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
import ru.beeline.capability.cleint.DashboardClient;
import ru.beeline.capability.cleint.UserClient;
import ru.beeline.capability.domain.*;
import ru.beeline.capability.dto.BusinessCapabilityShortDTO;
import ru.beeline.capability.dto.BusinessCapabilityTreeCustomDTO;
import ru.beeline.capability.dto.BusinessCapabilityTreeDTO;
import ru.beeline.capability.dto.CapabilityParentDTO;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.exception.ValidationException;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.mapper.BusinessCapabilityMapper;
import ru.beeline.capability.repository.*;
import ru.beeline.capability.utils.UrlWrapper;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenDTO;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenIdsDTO;
import ru.beeline.fdmlib.dto.capability.PutBusinessCapabilityDTO;

import java.util.*;
import java.util.stream.Collectors;

import static ru.beeline.capability.utils.Constants.*;

@Slf4j
@Service
@Transactional
public class BusinessCapabilityService {

    @Autowired
    private FindNameSortTableRepository findNameSortTableRepository;

    @Autowired
    private EntityTypeRepository entityTypeRepository;

    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;

    @Autowired
    private TechCapabilityRelationsRepository techCapabilityRelationsRepository;

    @Autowired
    private FindNameSortTableService findNameSortTableService;

    @Autowired
    private DashboardClient dashboardClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private BusinessCapabilityMapper businessCapabilityMapper;

    @Value("${queue.change-business-capability.name}")
    private String changeBusinessCapabilityQueueName;

    @Autowired
    private HistoryBusinessCapabilityRepository historyBusinessCapabilityRepository;

    public BusinessCapability findById(Long id) {
        return businessCapabilityRepository.findById(id).orElseThrow(() -> new NotFoundException("Business Capability не найдено"));
    }

    public BusinessCapabilityChildrenDTO getChildren(Long id) {
        BusinessCapability businessCapability = findById(id);
        if (businessCapability.getDeletedDate() != null) {
            throw new NotFoundException("Business Capability не найдено");
        }
        List<TechCapability> techCapabilities = businessCapability.getChildren().stream().map(TechCapabilityRelations::getTechCapability).filter(techCapability -> Objects.isNull(techCapability.getDeletedDate())).collect(Collectors.toList());
        List<BusinessCapability> businessCapabilitiesKids = businessCapabilityRepository.findAllByParentIdAndDeletedDateIsNull(id);
        return businessCapabilityMapper.convert(techCapabilities, businessCapabilitiesKids);
    }

    public BusinessCapabilityChildrenIdsDTO getChildrenIds(Long id) {
        List<Long> bcIds = new ArrayList<>();
        List<Long> tcIds = new ArrayList<>();
        BusinessCapability businessCapability = findById(id);
        businessCapability.setChildrenOfTree(getChildrenBC(businessCapability));
        tcIds.addAll(businessCapability.getChildren().stream().map(TechCapabilityRelations::getTechCapability).map(TechCapability::getId).collect(Collectors.toList()));
        businessCapability.getChildrenOfTree().forEach(childBc -> getTechCapabilities(childBc, bcIds, tcIds));
        return new BusinessCapabilityChildrenIdsDTO(tcIds, bcIds);
    }

    public void getTechCapabilities(BusinessCapability bc, List<Long> bcIds, List<Long> tcIds) {
        bc.setChildrenOfTree(getChildrenBC(bc));
        bcIds.add(bc.getId());
        tcIds.addAll(bc.getChildren().stream().map(TechCapabilityRelations::getTechCapability).map(TechCapability::getId).collect(Collectors.toList()));
        bc.getChildrenOfTree().forEach(childBc -> getTechCapabilities(childBc, bcIds, tcIds));
    }

    public CapabilityParentDTO getParents(Long id) {
        ArrayList<Long> result = new ArrayList<>();
        while (true) {
            id = findById(id).getParentId();

            if (Objects.isNull(id)) {
                return new CapabilityParentDTO(result);
            } else {
                result.add(id);
            }
        }
    }

    public CapabilityParentDTO getParentsWithoutDeleteDate(Long id) {
        ArrayList<Long> result = new ArrayList<>();
        BusinessCapability businessCapability = findById(id);
        if (businessCapability.getDeletedDate() != null) {
            throw new NotFoundException("Business Capability не найдено");
        }
        while (true) {
            Long parentId = businessCapability.getParentId();
            if (Objects.isNull(parentId)) {
                return new CapabilityParentDTO(result);
            } else {
                result.add(parentId);
                businessCapability = businessCapability.getParentEntity();
            }
        }
    }

    public BusinessCapabilityShortDTO getById(Long id) {
        BusinessCapability businessCapability = findById(id);

        if (businessCapability.getDeletedDate() != null) {
            throw new NotFoundException("Business Capability не найдено");
        }

        if (businessCapability.getParentEntity() != null && businessCapability.getParentEntity().getDeletedDate() != null)
            businessCapability.setParentEntity(null);

        return businessCapabilityMapper.convert(businessCapability, checkHasKids(businessCapability));
    }

    public List<BusinessCapability> getByIdIn(List<Long> ids) {
        return businessCapabilityRepository.findAllByIdIn(ids).stream().filter(bc -> bc.getDeletedDate() == null).collect(Collectors.toList());
    }

    private boolean checkHasKids(BusinessCapability businessCapability) {
        List<TechCapabilityRelations> techCapabilityRelations = techCapabilityRelationsRepository.findByBusinessCapability(businessCapability);
        if (!techCapabilityRelations.isEmpty()) {
            if (techCapabilityRelations.stream()
                    .map(TechCapabilityRelations::getTechCapability)
                    .anyMatch(tech -> tech.getDeletedDate() == null))
                return true;
        }
        return !businessCapabilityRepository.findAllByParentIdAndDeletedDateIsNull(businessCapability.getId()).isEmpty();
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
                businessCapabilities.stream().filter(сapability -> Objects.nonNull(сapability.getParentEntity()) && Objects.nonNull(сapability.getParentEntity().getDeletedDate())).forEach(сapability -> сapability.setParentEntity(null));
                break;
            case CORE:
                businessCapabilities = businessCapabilityRepository.findCapabilitiesWithoutParent(pageable);
                break;
            default:
                throw new IllegalArgumentException("Invalid value for findBy: " + findBy);
        }

        return businessCapabilityMapper.convertToBusinessCapabilityShortDTOList(businessCapabilities.toList());
    }

    public void putCapability(PutBusinessCapabilityDTO capabilityDTO, String userId, String productIds, String roles, String permissions) {
        Optional<BusinessCapability> businessCapabilityOptional = businessCapabilityRepository.findByCode(capabilityDTO.getCode());
        BusinessCapability businessCapability;
        if (businessCapabilityOptional.isPresent()) {
            businessCapability = businessCapabilityOptional.get();
            capabilityDTO.setDescription(UrlWrapper.proxyUrl(capabilityDTO.getDescription()));
            if (!capabilityDTO.equals(businessCapabilityMapper.convertToPutCapabilityDTO(businessCapability))) {
                log.info("businessCapability from BD : " + businessCapability.toString());
                log.info("capabilityDTO from Dashboard: " + capabilityDTO.toString() + " Capability after Convert to PutCapability from bd: "
                        + businessCapabilityMapper.convertToPutCapabilityDTO(businessCapability).toString());
                addToHistory(businessCapability);
                businessCapability = updateCapability(businessCapability, capabilityDTO);
                sendNotify(businessCapability.getId(), UPDATE, changeBusinessCapabilityQueueName, capabilityDTO.getName());
                findNameSortTableService.updateVector(businessCapability.getId(), businessCapability.getName(), businessCapability.getDescription(), businessCapability.getCode(), ENTITY_TYPE_BUSINESS_CAPABILITY);
                putCapabilityToDashboard(capabilityDTO, userId, productIds, roles, permissions);
            }
        } else {
            businessCapability = createCapabilities(capabilityDTO);
            findNameSortTableService.updateVector(businessCapability.getId(), businessCapability.getName(), businessCapability.getDescription(), businessCapability.getCode(), ENTITY_TYPE_BUSINESS_CAPABILITY);
            putCapabilityToDashboard(capabilityDTO, userId, productIds, roles, permissions);
        }
    }

    private void addToHistory(BusinessCapability businessCapability) {
        Optional<HistoryBusinessCapability> historyBusinessCapability = historyBusinessCapabilityRepository.findTopByIdRefOrderByVersionDesc(businessCapability.getId());
        historyBusinessCapabilityRepository.save(HistoryBusinessCapability.builder()
                .idRef(businessCapability.getId())
                .version(historyBusinessCapability.isPresent() ? historyBusinessCapability.get().getVersion() + 1 : 1)
                .code(businessCapability.getCode())
                .name(businessCapability.getName())
                .description(businessCapability.getDescription())
                .modifiedDate(new Date())
                .owner(businessCapability.getOwner())
                .status(businessCapability.getStatus())
                .link(businessCapability.getLink())
                .author(businessCapability.getAuthor())
                .isDomain(businessCapability.isDomain())
                .build());
    }

    private void putCapabilityToDashboard(PutBusinessCapabilityDTO capabilityDTO, String userId, String productIds, String roles, String permissions) {
        if (Objects.nonNull(userId) && Objects.nonNull(productIds) && Objects.nonNull(roles) && Objects.nonNull(permissions)) {
            if (capabilityDTO.getAuthor() == null || capabilityDTO.getAuthor().isEmpty()) {
                fillAuthor(capabilityDTO, userId);
            }
            dashboardClient.putCapability(capabilityDTO);
        }
    }

    private void fillAuthor(PutBusinessCapabilityDTO capabilityDTO, String userId) {
        capabilityDTO.setAuthor(userClient.getEmail(userId));
    }

    private BusinessCapability updateCapability(BusinessCapability businessCapability, PutBusinessCapabilityDTO capabilityDTO) {
        businessCapability.setName(capabilityDTO.getName());
        businessCapability.setDescription(UrlWrapper.proxyUrl(capabilityDTO.getDescription()));
        businessCapability.setStatus(capabilityDTO.getStatus());
        businessCapability.setAuthor(capabilityDTO.getAuthor());
        businessCapability.setLastModifiedDate(new Date());
        businessCapability.setLink(capabilityDTO.getLink());
        businessCapability.setOwner(capabilityDTO.getOwner());
        businessCapability.setParentId(getParentId(capabilityDTO));
        businessCapability.setDomain(capabilityDTO.getIsDomain());
        return businessCapabilityRepository.save(businessCapability);
    }

    private BusinessCapability createCapabilities(PutBusinessCapabilityDTO capability) {

        BusinessCapability result = businessCapabilityRepository.save(
                BusinessCapability.builder()
                        .code(capability.getCode())
                        .name(capability.getName())
                        .description(UrlWrapper.proxyUrl(capability.getDescription()))
                        .status(capability.getStatus())
                        .author(capability.getAuthor())
                        .createdDate(new Date()).lastModifiedDate(new Date())
                        .link(capability.getLink())
                        .owner(capability.getOwner())
                        .parentId(getParentId(capability))
                        .isDomain(capability.getIsDomain())
                        .build());
        sendNotify(result.getId(), CREATE, changeBusinessCapabilityQueueName, capability.getName());
        return result;
    }

    private Long getParentId(PutBusinessCapabilityDTO capability) {
        if (capability == null || capability.getParent() == null) return null;
        return businessCapabilityRepository.findByCode(capability.getParent()).map(BusinessCapability::getId).orElse(null);
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

    public void sendMessageToTechCapabilityQueue(String queue, String message) {
        rabbitTemplate.convertAndSend(queue, message, messagePostProcessor -> {
            messagePostProcessor.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return messagePostProcessor;
        });
    }

    public BusinessCapabilityTreeCustomDTO getBusinessCapabilityTreeById(Long id) {
        BusinessCapability bc = findById(id);
        if (bc.getDeletedDate() != null) {
            throw new NotFoundException("Business Capability не найдено");
        }
        bc.setChildrenOfTree(getChildrenBC(bc));
        List<BusinessCapability> filteredBusinessCapabilities = bc.getChildrenOfTree();
        filteredBusinessCapabilities.forEach(businessCapability ->
                businessCapability.setChildrenOfTree(
                        filterChildren(businessCapability.getChildrenOfTree(), businessCapability.isDomain())));
        return businessCapabilityMapper.mapToCustomTree(filteredBusinessCapabilities, bc);

    }

    public List<BusinessCapabilityTreeDTO> getBusinessCapabilityTree() {
        List<BusinessCapability> businessCapabilities =
                businessCapabilityRepository.findAllByParentIdIsNullAndDeletedDateIsNullAndIsDomainIsTrue();
        return businessCapabilityMapper.mapToTree(filterChildrenWithDomainIsTrue(businessCapabilities));
    }

    private List<BusinessCapability> filterChildren(List<BusinessCapability> children, boolean isDomain) {
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        children.forEach(bc -> bc.setChildrenOfTree(getChildrenBC(bc)));
        return children.stream()
                .filter(businessCapability -> businessCapability.getDeletedDate() == null
                        && businessCapability.isDomain() == isDomain)
                .peek(businessCapability ->
                        businessCapability.setChildrenOfTree(
                                filterChildren(businessCapability.getChildrenOfTree(), isDomain)))
                .collect(Collectors.toList());
    }

    private List<BusinessCapability> filterChildrenWithDomainIsTrue(List<BusinessCapability> children) {
        children.forEach(bc -> bc.setChildrenOfTree(getChildrenBC(bc)));
        return children.stream()
                .filter(businessCapability -> businessCapability.getDeletedDate() == null
                        && businessCapability.isDomain())
                .peek(businessCapability ->
                        businessCapability.setChildrenOfTree(
                                filterChildrenWithDomainIsTrue(businessCapability.getChildrenOfTree())))
                .collect(Collectors.toList());
    }


    public void validateBusinessCapabilityDTO(PutBusinessCapabilityDTO capabilityDTO, String userId, String productIds, String roles, String permissions) {
        StringBuilder errMsg = new StringBuilder();
        if (capabilityDTO.getCode() == null || capabilityDTO.getCode().isEmpty()) {
            if (Objects.nonNull(userId) && Objects.nonNull(productIds) && Objects.nonNull(roles) && Objects.nonNull(permissions)) {
                capabilityDTO.setCode(getPrefix(capabilityDTO) + Long.toString(businessCapabilityRepository.findFirstByOrderByIdDesc().getId() + 1));
            } else {
                errMsg.append("Отсутствует обязательное поле code\n");
            }
        }
        if (!capabilityDTO.getIsDomain() && (capabilityDTO.getParent() == null || capabilityDTO.getParent().isEmpty())) {
            errMsg.append("Отсутствует обязательное поле parent\n");
        }
        if (capabilityDTO.getName() == null) {
            errMsg.append("Отсутствует обязательное поле name\n");
        }
        if (capabilityDTO.getAuthor() == null) {
            errMsg.append("Отсутствует обязательное поле author\n");
        }
        if (capabilityDTO.getCode().equals(capabilityDTO.getParent())) {
            errMsg.append("Возможность не может быть собственным родителем\n");
        }

        if (!errMsg.toString().isEmpty()) {
            throw new ValidationException(errMsg.toString());
        }
    }

    private String getPrefix(PutBusinessCapabilityDTO businessCapability) {
        String prefix;
        if (!businessCapability.getIsDomain()) {
            prefix = "BC.";
        } else {
            if (businessCapability.getParent() == null || businessCapability.getParent().isEmpty()) {
                prefix = "GRP.";
            } else {
                prefix = "DMN.";
            }
        }
        return prefix;
    }

    public List<BusinessCapability> getChildrenBC(BusinessCapability businessCapability) {
        return businessCapabilityRepository.findAllByParentId(businessCapability.getId());
    }

    enum FindBy {
        ALL, CORE
    }

    public void deleteBusinessCapability(String code) {
        Optional<BusinessCapability> optionalBusinessCapability = businessCapabilityRepository.findByCode(code);
        if (optionalBusinessCapability.isPresent()) {
            if (optionalBusinessCapability.get().getDeletedDate() == null) {
                Long businessCapabilityId = optionalBusinessCapability.get().getId();
                optionalBusinessCapability.map(businessCapability -> {
                    businessCapability.setDeletedDate(new Date());
                    return businessCapabilityRepository.save(businessCapability);
                });
                EntityType entityType = entityTypeRepository.findByName("BUSINESS_CAPABILITY");
                findNameSortTableRepository.deleteByRefIdAndType(businessCapabilityId, entityType);
            }
        }
    }
}
