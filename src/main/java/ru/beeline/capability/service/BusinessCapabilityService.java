package ru.beeline.capability.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.beeline.capability.client.DashboardClient;
import ru.beeline.capability.client.UserClient;
import ru.beeline.capability.domain.*;
import ru.beeline.capability.dto.*;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.exception.ValidationException;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.mapper.BusinessCapabilityMapper;
import ru.beeline.capability.repository.*;
import ru.beeline.capability.utils.Node;
import ru.beeline.capability.utils.UrlWrapper;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenDTO;
import ru.beeline.fdmlib.dto.capability.BusinessCapabilityChildrenIdsDTO;
import ru.beeline.fdmlib.dto.capability.PutBusinessCapabilityDTO;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static ru.beeline.capability.utils.Constants.ENTITY_TYPE_BUSINESS_CAPABILITY;

@Slf4j
@Service
@Transactional
public class BusinessCapabilityService {

    @Value("${queue.error-bc-queue.name}")
    private String errorBcQueueName;

    @Autowired
    RabbitService rabbitService;

    @Autowired
    private UserClient userClient;

    @Autowired
    private DashboardClient dashboardClient;

    @Autowired
    private EntityTypeRepository entityTypeRepository;

    @Autowired
    private FindNameSortTableService findNameSortTableService;

    @Autowired
    private BusinessCapabilityMapper businessCapabilityMapper;

    @Autowired
    private FindNameSortTableRepository findNameSortTableRepository;

    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;

    @Autowired
    private TechCapabilityRelationsRepository techCapabilityRelationsRepository;

    @Autowired
    private OrderBusinessCapabilityRepository orderBusinessCapabilityRepository;

    @Autowired
    private HistoryBusinessCapabilityRepository historyBusinessCapabilityRepository;

    public BusinessCapability findById(Long id) {
        return businessCapabilityRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Business Capability с id: " + id + " не найдено"));
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

    private Map<Long, Node> getNodeMap() {
        Map<Long, Node> nodeMap = new HashMap<>();
        for (BusinessCapability obj : businessCapabilityRepository.findAll()) {
            if (obj.getDeletedDate() == null) {
                Node node = new Node(obj.getId(), obj.getParentId(), obj);
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
        return nodeMap;
    }

    public BusinessCapabilityChildrenIdsDTO getChildrenIds(Long id) {
        List<Long> bcIds = new ArrayList<>();
        List<Long> tcIds = new ArrayList<>();
        Map<Long, Node> bcMap = getNodeMap();

        BusinessCapability businessCapability = findById(id);
        businessCapability.setChildrenOfTree(bcMap.get(businessCapability.getId()).getChildren().stream().map(Node::getBusinessCapability).collect(Collectors.toList()));
        tcIds.addAll(businessCapability.getChildren().stream().map(TechCapabilityRelations::getTechCapability)
                .map(TechCapability::getId).collect(Collectors.toList()));
        businessCapability.getChildrenOfTree().forEach(childBc -> getTechCapabilities(childBc, bcIds, tcIds, bcMap));

        Set<Long> tcIdsSet = new HashSet<>(tcIds);
        List<Long> tcIdsWithoutDuplicates = new ArrayList<>(tcIdsSet);
        return new BusinessCapabilityChildrenIdsDTO(tcIdsWithoutDuplicates, bcIds);
    }

    public void getTechCapabilities(BusinessCapability bc, List<Long> bcIds, List<Long> tcIds, Map<Long, Node> bcMap) {
        bc.setChildrenOfTree(bcMap.get(bc.getId()).getChildren().stream().map(Node::getBusinessCapability).collect(Collectors.toList()));
        bcIds.add(bc.getId());
        tcIds.addAll(bc.getChildren().stream().map(TechCapabilityRelations::getTechCapability).map(TechCapability::getId).collect(Collectors.toList()));
        bc.getChildrenOfTree().forEach(childBc -> getTechCapabilities(childBc, bcIds, tcIds, bcMap));
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
        BusinessCapability businessCapability = businessCapabilityRepository.findByIdAndDeletedDateIsNull(id).orElseThrow(() ->
                new NotFoundException("Business Capability с id: " + id + " не найдено"));
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
        return businessCapabilityRepository.findAllByIdInAndDeletedDateIsNull(ids);
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
        Pageable pageable = new OffsetBasedPageRequest(offset, limit == null || limit == 0 ? Integer.MAX_VALUE : limit,
                Sort.by(Sort.Direction.ASC, "name"));
        Page<BusinessCapability> businessCapabilities;
        switch (findBy) {
            case "ALL":
                businessCapabilities = businessCapabilityRepository.findCapabilities(pageable);
                break;
            case "CORE":
                businessCapabilities = businessCapabilityRepository.findCapabilitiesWithoutParent(pageable);
                break;
            case "DOMAIN":
                businessCapabilities = businessCapabilityRepository.findByIsDomainTrueAndDeletedDateIsNull(pageable);
                break;
            default:
                throw new IllegalArgumentException("Unsupported FindBy value");
        }
        return businessCapabilityMapper.convertToBusinessCapabilityShortDTOList(businessCapabilities.toList(), findBy);
    }

    public void putCapability(PutBusinessCapabilityDTO capabilityDTO, String userId, String productIds, String roles,
                              String permissions, String source) {
        if (source == null || source.isEmpty()) {
            source = "Sparx";
        }
        Optional<BusinessCapability> businessCapabilityOptional = businessCapabilityRepository.findByCode(capabilityDTO.getCode());
        BusinessCapability businessCapability;
        if (businessCapabilityOptional.isPresent()) {
            businessCapability = businessCapabilityOptional.get();
            capabilityDTO.setDescription(UrlWrapper.proxyUrl(capabilityDTO.getDescription()));
            if (capabilityDTO.getAuthor() == null || capabilityDTO.getAuthor().isEmpty()) {
                capabilityDTO.setAuthor("Sparx EA");
            }
            boolean shouldUpdate = !capabilityDTO.equals(businessCapabilityMapper.convertToPutCapabilityDTO(businessCapability)) ||
                    (capabilityDTO.equals(businessCapabilityMapper.convertToPutCapabilityDTO(businessCapability)) &&
                            businessCapability.getDeletedDate() != null);
            if (shouldUpdate || !source.equals(businessCapability.getSource())) {
                log.info("businessCapability from BD : " + businessCapability.toString());
                log.info("capabilityDTO from Dashboard: " + capabilityDTO.toString() + " Capability after Convert to PutCapability from bd: "
                        + businessCapabilityMapper.convertToPutCapabilityDTO(businessCapability).toString());
                addToHistory(businessCapability);
                businessCapability = updateCapability(businessCapability, capabilityDTO, source);
                findNameSortTableService.updateVector(businessCapability.getId(), businessCapability.getName(),
                        businessCapability.getDescription(), businessCapability.getCode(), ENTITY_TYPE_BUSINESS_CAPABILITY);
                putCapabilityToDashboard(capabilityDTO, userId, productIds, roles, permissions);
            }
        } else {
            businessCapability = createCapabilities(capabilityDTO, source);
            if (!areParametersValid(userId, productIds, roles, permissions)) {
                findNameSortTableService.updateVector(businessCapability.getId(), businessCapability.getName(),
                        businessCapability.getDescription(), businessCapability.getCode(), ENTITY_TYPE_BUSINESS_CAPABILITY);
                log.warn("One or more required parameters are null or empty. Business capability  has been preserved.");
            } else {
                if (putCapabilityToDashboard(capabilityDTO, userId, productIds, roles, permissions) != null) {
                    findNameSortTableService.updateVector(businessCapability.getId(), businessCapability.getName(),
                            businessCapability.getDescription(), businessCapability.getCode(), ENTITY_TYPE_BUSINESS_CAPABILITY);
                } else {
                    businessCapabilityRepository.delete(businessCapability);
                }
            }
        }
    }

    private boolean areParametersValid(String userId, String productIds, String roles, String permissions) {
        return userId != null && !userId.isEmpty() &&
                productIds != null && !productIds.isEmpty() &&
                roles != null && !roles.isEmpty() &&
                permissions != null && !permissions.isEmpty();
    }

    private void addToHistory(BusinessCapability businessCapability) {
        Optional<HistoryBusinessCapability> historyBusinessCapability = historyBusinessCapabilityRepository.findTopByIdRefOrderByVersionDesc(businessCapability.getId());
        historyBusinessCapabilityRepository.save(HistoryBusinessCapability.builder()
                .idRef(businessCapability.getId())
                .version(historyBusinessCapability.isPresent() ? historyBusinessCapability.get().getVersion() + 1 : 1)
                .code(businessCapability.getCode())
                .name(businessCapability.getName())
                .description(businessCapability.getDescription())
                .modifiedDate(businessCapability.getLastModifiedDate() == null ?
                        businessCapability.getCreatedDate() : businessCapability.getLastModifiedDate())
                .parentId(businessCapability.getParentId())
                .owner(businessCapability.getOwner())
                .status(businessCapability.getStatus())
                .link(businessCapability.getLink())
                .author(businessCapability.getAuthor())
                .isDomain(businessCapability.isDomain())
                .deletedDate(businessCapability.getDeletedDate())
                .source(businessCapability.getSource())
                .build());
    }

    private String putCapabilityToDashboard(PutBusinessCapabilityDTO capabilityDTO, String userId, String productIds, String roles, String permissions) {
        if (Objects.nonNull(userId) && Objects.nonNull(productIds) && Objects.nonNull(roles) && Objects.nonNull(permissions)) {
            if (capabilityDTO.getAuthor() == null || capabilityDTO.getAuthor().isEmpty()) {
                fillAuthor(capabilityDTO, userId);
            }
            return dashboardClient.putCapability(capabilityDTO);
        }
        return null;
    }

    private void fillAuthor(PutBusinessCapabilityDTO capabilityDTO, String userId) {
        capabilityDTO.setAuthor(userClient.getEmail(userId));
    }

    private BusinessCapability updateCapability(BusinessCapability businessCapability, PutBusinessCapabilityDTO capabilityDTO,
                                                String source) {
        businessCapability.setName(capabilityDTO.getName());
        businessCapability.setDescription(UrlWrapper.proxyUrl(capabilityDTO.getDescription()));
        businessCapability.setStatus(capabilityDTO.getStatus());
        businessCapability.setAuthor(capabilityDTO.getAuthor() == null || capabilityDTO.getAuthor().isEmpty() ?
                "Sparx EA" : capabilityDTO.getAuthor());
        businessCapability.setLastModifiedDate(new Date());
        businessCapability.setDeletedDate(null);
        businessCapability.setLink(capabilityDTO.getLink());
        businessCapability.setOwner(capabilityDTO.getOwner());
        businessCapability.setParentId(getParentId(capabilityDTO));
        businessCapability.setDomain(capabilityDTO.getIsDomain());
        businessCapability.setSource(source);
        return businessCapabilityRepository.save(businessCapability);
    }

    private BusinessCapability createCapabilities(PutBusinessCapabilityDTO capability, String source) {

        BusinessCapability result = businessCapabilityRepository.save(
                BusinessCapability.builder()
                        .code(capability.getCode())
                        .name(capability.getName())
                        .description(UrlWrapper.proxyUrl(capability.getDescription()))
                        .status(capability.getStatus())
                        .author(capability.getAuthor() == null || capability.getAuthor().isEmpty() ?
                                "Sparx EA" : capability.getAuthor())
                        .createdDate(new Date())
                        .link(capability.getLink())
                        .owner(capability.getOwner())
                        .parentId(getParentId(capability))
                        .isDomain(capability.getIsDomain())
                        .source(source == null || source.isEmpty() ? "Sparx" : source)
                        .build());
        return result;
    }

    private Long getParentId(PutBusinessCapabilityDTO capability) {
        if (capability == null || capability.getParent() == null) return null;
        return businessCapabilityRepository.findByCode(capability.getParent()).map(BusinessCapability::getId).orElse(null);
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

    public void postBusinessCapability(Integer id) {
        OrderBusinessCapability orderBusinessCapability = orderBusinessCapabilityRepository.findById(id).orElseThrow(() -> new NotFoundException("OrderBusinessCapability не найдено"));
        BusinessCapability businessCapability;
        if (orderBusinessCapability.getMutableBcId() != null) {
            businessCapability = findById(orderBusinessCapability.getMutableBcId());
            businessCapability.setLastModifiedDate(new Date());
        } else {
            Optional<BusinessCapability> bcByCode = businessCapabilityRepository.findByCode(orderBusinessCapability.getCode());
            if (bcByCode.isPresent()) {
                throw new IllegalArgumentException("BC уже создана");
            }
            businessCapability = new BusinessCapability();
            businessCapability.setCode(orderBusinessCapability.getCode());
            businessCapability.setCreatedDate(Date.from(Instant.now()));

        }
        businessCapability.setSource("FDM");
        businessCapability.setLink(null);
        businessCapability.setName(orderBusinessCapability.getName());
        businessCapability.setDescription(orderBusinessCapability.getDescription());
        businessCapability.setParentId(Long.parseLong(orderBusinessCapability.getParentId().toString()));
        businessCapability.setOwner(orderBusinessCapability.getOwner());
        businessCapability.setAuthor(orderBusinessCapability.getAuthor());
        businessCapability.setStatus(orderBusinessCapability.getStatus());
        businessCapability.setDeletedDate(null);
        businessCapability = businessCapabilityRepository.save(businessCapability);
        EntityType entityType = entityTypeRepository.findByName("BUSINESS_CAPABILITY");
        findNameSortTableRepository.findByRefIdAndType(businessCapability.getId(), entityType);
        findNameSortTableService.updateVector(businessCapability.getId(), businessCapability.getName(),
                businessCapability.getDescription(), businessCapability.getCode(), ENTITY_TYPE_BUSINESS_CAPABILITY);
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

    public List<GetHistoryByIdDTO> getBusinessCapabilityHistory(Long id) {
        BusinessCapability businessCapability = findById(id);
        List<HistoryBusinessCapability> historyBcList = historyBusinessCapabilityRepository.findByIdRef(id);
        VersionInfoDTO versionInfo = VersionInfoDTO.builder()
                .version(1)
                .modified_date(businessCapability.getLastModifiedDate() == null ?
                        businessCapability.getCreatedDate() : businessCapability.getLastModifiedDate())
                .author(businessCapability.getAuthor())
                .build();
        if (historyBcList.isEmpty()) {
            return List.of(GetHistoryByIdDTO.builder()
                    .versionInfo(versionInfo)
                    .build());
        } else {
            List<VersionInfoDTO> versionInfoList = historyBcList.stream()
                    .map(historyBc -> VersionInfoDTO.builder()
                            .version(historyBc.getVersion().intValue())
                            .modified_date(historyBc.getModifiedDate())
                            .author(historyBc.getAuthor())
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

    public List<GetBcHistoryVersionDTO> getBusinessCapabilityHistoryVersion(Long id, Integer version,
                                                                            Integer otherVersion) {

        BusinessCapability businessCapability = findById(id);
        HistoryBusinessCapability historyBcFirstVersion = findHistoryBcVersion(id, version.longValue());
        List<HistoryCapabilityDTO> result = new ArrayList<>();
        result.add(buildBcHistoryVersionDTO(historyBcFirstVersion, id, version));
        if (otherVersion != null) {
            HistoryBusinessCapability historyBcSecondVersion = findHistoryBcVersion(id, otherVersion.longValue());
            result.add(buildBcHistoryVersionDTO(historyBcSecondVersion, id, otherVersion));
        } else {
            Optional<HistoryBusinessCapability> optionalFindHistoryBcOtherVersion =
                    historyBusinessCapabilityRepository.findByIdRefOtherVersion(id);
            if (optionalFindHistoryBcOtherVersion.isPresent()) {
                HistoryCapabilityDTO getHistoryCapabilityDTO = HistoryCapabilityDTO.builder()
                        .id(id)
                        .code(businessCapability.getCode())
                        .name(businessCapability.getName())
                        .description(businessCapability.getDescription())
                        .owner(businessCapability.getOwner())
                        .modifiedDate(businessCapability.getLastModifiedDate())
                        .deletedDate(businessCapability.getDeletedDate())
                        .status(businessCapability.getStatus())
                        .parent(findParentBc(businessCapability.getParentId()))
                        .author(businessCapability.getAuthor())
                        .link(businessCapability.getLink())
                        .version(optionalFindHistoryBcOtherVersion.get().getVersion().intValue() + 1)
                        .build();
                result.add(getHistoryCapabilityDTO);
            } else {
                throw new NotFoundException("History Business Capability с последней версией не найдено");
            }
        }

        result.sort(Comparator.comparingInt(HistoryCapabilityDTO::getVersion).reversed());
        return result.stream()
                .map(capability -> GetBcHistoryVersionDTO.builder()
                        .capability(capability)
                        .build())
                .collect(Collectors.toList());
    }

    private HistoryBusinessCapability findHistoryBcVersion(Long id, Long version) {
        Optional<HistoryBusinessCapability> optionalHistoryBc =
                historyBusinessCapabilityRepository.findByIdRefAndVersion(id, version);
        if (optionalHistoryBc.isEmpty()) {
            throw new NotFoundException(String.format("History Business Capability с id: %d, version: %s не найдено",
                    id, version));
        }
        return optionalHistoryBc.get();
    }

    private HistoryCapabilityDTO buildBcHistoryVersionDTO(HistoryBusinessCapability historyBusinessCapability,
                                                          Long id, Integer version) {
        ParentDTO parentDTO = null;
        if (historyBusinessCapability.getParentId() != null) {
            parentDTO = findParentBc(historyBusinessCapability.getParentId());
        }
        return buildBcHistoryVersion(historyBusinessCapability, parentDTO, id, version);
    }

    private HistoryCapabilityDTO buildBcHistoryVersion(HistoryBusinessCapability historyBusinessCapability,
                                                       ParentDTO parentDTO, Long id, Integer version) {
        return HistoryCapabilityDTO.builder()
                .id(id)
                .code(historyBusinessCapability.getCode())
                .name(historyBusinessCapability.getName())
                .description(historyBusinessCapability.getDescription())
                .owner(historyBusinessCapability.getOwner())
                .modifiedDate(historyBusinessCapability.getModifiedDate())
                .deletedDate(historyBusinessCapability.getDeletedDate())
                .status(historyBusinessCapability.getStatus())
                .parent(parentDTO)
                .author(historyBusinessCapability.getAuthor())
                .link(historyBusinessCapability.getLink())
                .version(version)
                .build();
    }

    private ParentDTO findParentBc(Long parentId) {
        Optional<BusinessCapability> optionalParentFirstBc = businessCapabilityRepository.findById(parentId);
        if (optionalParentFirstBc.isPresent()) {
            BusinessCapability parentFirstBc = optionalParentFirstBc.get();
            return ParentDTO.builder()
                    .id(parentId)
                    .code(parentFirstBc.getCode())
                    .name((parentFirstBc.getName()))
                    .build();
        } else {
            throw new NotFoundException(String.format("Business Capability с parentId: %s не найдено", parentId));
        }
    }

    public void processMessage(JsonNode jsonNode) {
        String source = jsonNode.get("source").asText();
        String changeType = jsonNode.get("changeType").asText();
        Long entityId = jsonNode.get("entityId").asLong();
        if (!source.equals("Sparx") && !source.equals("SparxEA") && !changeType.equals("DELETE")) {
            Optional<BusinessCapability> capabilityOpt = businessCapabilityRepository.findById(entityId);
            if (capabilityOpt.isEmpty()) {
                log.error("BusinessCapability с id {} не найдено", entityId);
                return;
            }
            BusinessCapability BusinessCapability = capabilityOpt.get();
            PutBusinessCapabilityDTO putBusinessCapabilityDTO = businessCapabilityMapper.convertToPutCapabilityDTO(BusinessCapability);

            if (dashboardClient.putCapability(putBusinessCapabilityDTO) != null) {
                log.info("BusinessCapability: {} успешно отправлено в Dashboard", BusinessCapability.getCode());
            } else {
                log.error("Ошибка при отправке BusinessCapability: {} в Dashboard", BusinessCapability.getCode());
                log.info("Сообщение ,будет отправлено в очередь error_bc_entry_in_sparx");
                rabbitService.sendMessage(errorBcQueueName, jsonNode);
            }
        } else {
            log.info("Сообщение не прошло по условию.");
        }
    }
}

