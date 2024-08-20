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
import ru.beeline.capability.cleint.DashboardClient;
import ru.beeline.capability.cleint.UserClient;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;
import ru.beeline.capability.dto.BusinessCapabilityShortDTO;
import ru.beeline.capability.dto.BusinessCapabilityTreeDTO;
import ru.beeline.capability.dto.CapabilityParentDTO;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.exception.ValidationException;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.mapper.BusinessCapabilityMapper;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.TechCapabilityRelationsRepository;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenDTO;
import ru.beeline.fdmlib.dto.capability.PutBusinessCapabilityDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private DashboardClient dashboardClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private BusinessCapabilityMapper businessCapabilityMapper;

    @Value("${queue.change-business-capability.name}")
    private String changeBusinessCapabilityQueueName;


    public BusinessCapabilityChildrenDTO getChildren(Long id) {
        List<TechCapability> techCapabilities = businessCapabilityRepository.findById(id).orElseThrow(() -> new NotFoundException("Business Capability не найдено")).getChildren().stream().map(TechCapabilityRelations::getTechCapability).filter(techCapability -> Objects.isNull(techCapability.getDeletedDate())).collect(Collectors.toList());
        List<BusinessCapability> businessCapabilitiesKids = businessCapabilityRepository.findAllByParentIdAndDeletedDateIsNull(id);
        return businessCapabilityMapper.convert(techCapabilities, businessCapabilitiesKids);
    }

    public CapabilityParentDTO getParents(Long id) {
        ArrayList<Long> result = new ArrayList<>();
        while (true) {
            id = businessCapabilityRepository.findById(id).orElseThrow(() -> new NotFoundException("Business Capability не найдено")).getParentId();

            if (Objects.isNull(id)) {
                return new CapabilityParentDTO(result);
            } else {
                result.add(id);
            }
        }
    }

    public BusinessCapabilityShortDTO getById(Long id) {
        BusinessCapability businessCapability = businessCapabilityRepository.findById(id).orElseThrow(() -> new NotFoundException("Business Capability не найдено"));

        if (businessCapability.getParentEntity() != null && businessCapability.getParentEntity().getDeletedDate() != null)
            businessCapability.setParentEntity(null);

        return businessCapabilityMapper.convert(businessCapability, checkHasKids(businessCapability));
    }

    public List<BusinessCapability> getByIdIn(List<Long> ids) {
        return businessCapabilityRepository.findAllByIdIn(ids).stream().filter(bc -> bc.getDeletedDate() == null).collect(Collectors.toList());
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
            if (!capabilityDTO.equals(businessCapabilityMapper.convertToPutCapabilityDTO(businessCapability))) {
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
        businessCapability.setDescription(proxyUrl(capabilityDTO.getDescription()));
        businessCapability.setStatus(capabilityDTO.getStatus());
        businessCapability.setAuthor(capabilityDTO.getAuthor());
        businessCapability.setLastModifiedDate(new Date());
        businessCapability.setLink(capabilityDTO.getLink());
        businessCapability.setOwner(capabilityDTO.getOwner());
        businessCapability.setParentId(getParentId(capabilityDTO));
        businessCapability.setDomain(capabilityDTO.getIsDomain());
        return businessCapabilityRepository.save(businessCapability);
    }

    public String proxyUrl(String description) {
        String urlPattern = "\\b(https?://\\S+)\\b";
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher(description);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String url = matcher.group(1);
            if (!description.contains("<a href=\"" + url) && !description.contains(">" + url)) {
                String replacement = "<a href=\"" + url + "\">" + url + "</a>";
                matcher.appendReplacement(result, replacement);
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private BusinessCapability createCapabilities(PutBusinessCapabilityDTO capability) {

        BusinessCapability result = businessCapabilityRepository.save(
                BusinessCapability.builder()
                        .code(capability.getCode())
                        .name(capability.getName())
                        .description(proxyUrl(capability.getDescription()))
                        .status(capability.getStatus())
                        .author(capability.getAuthor())
                        .createdDate(new Date())
                        .lastModifiedDate(new Date())
                        .link(capability.getLink())
                        .owner(capability.getOwner())
                        .parentId(getParentId(capability))
                        .isDomain(capability.getIsDomain()).build());
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

    public List<BusinessCapabilityTreeDTO> getBusinessCapabilityTree(Long id) {
        List<BusinessCapabilityTreeDTO> result;
        if (id == null) {
            List<BusinessCapability> businessCapabilities = businessCapabilityRepository.findAllByParentIdIsNullAndDeletedDateIsNullAndIsDomainIsTrue();
            List<BusinessCapability> filteredBusinessCapabilities = filterChildrenWithDomainIsTrue(businessCapabilities);
            result = businessCapabilityMapper.mapToTree(filteredBusinessCapabilities);
        } else {
            Optional<BusinessCapability> businessCapabilitiesOptional = businessCapabilityRepository.findById(id);
            if (businessCapabilitiesOptional.isPresent()) {
                List<BusinessCapability> filteredBusinessCapabilities = filterChildren(Arrays.asList(businessCapabilitiesOptional.get()), businessCapabilitiesOptional.get().isDomain());
                result = businessCapabilityMapper.mapToTree(filteredBusinessCapabilities);
            } else {
                result = new ArrayList<>();
            }
        }
        return result;
    }

    private List<BusinessCapability> filterChildren(List<BusinessCapability> children, boolean isDomain) {
        return children.stream().filter(businessCapability -> businessCapability.getDeletedDate() == null && businessCapability.isDomain() == isDomain).peek(businessCapability -> businessCapability.setChildrenOfTree(filterChildren(businessCapability.getChildrenOfTree(), isDomain))).collect(Collectors.toList());
    }

    private List<BusinessCapability> filterChildrenWithDomainIsTrue(List<BusinessCapability> children) {
        return children.stream().filter(businessCapability -> businessCapability.getDeletedDate() == null && businessCapability.isDomain()).peek(businessCapability -> businessCapability.setChildrenOfTree(filterChildrenWithDomainIsTrue(businessCapability.getChildrenOfTree()))).collect(Collectors.toList());
    }

    enum FindBy {
        ALL, CORE
    }

    public void validateBusinessCapabilityDTO(PutBusinessCapabilityDTO capabilityDTO, String userId, String productIds, String roles, String permissions) {
        StringBuilder errMsg = new StringBuilder();
        if (capabilityDTO.getCode() == null) {
            if (Objects.nonNull(userId) && Objects.nonNull(productIds) && Objects.nonNull(roles) && Objects.nonNull(permissions)) {
                capabilityDTO.setCode("BC" + Long.toString(businessCapabilityRepository.findFirstByOrderByIdDesc().getId() + 1));
            } else {
                errMsg.append("Отсутсвует обязательное поле code\n");
            }
        }
        if (capabilityDTO.getName() == null) {
            errMsg.append("Отсутсвует обязательное поле name\n");
        }
        if (capabilityDTO.getAuthor() == null) {
            errMsg.append("Отсутсвует обязательное поле author\n");
        }
        if (capabilityDTO.getCode().equals(capabilityDTO.getParent())) {
            errMsg.append("Возможность не может быть собственным родителем\n");
        }

        if (!errMsg.toString().isEmpty()) {
            throw new ValidationException(errMsg.toString());
        }
    }

}
