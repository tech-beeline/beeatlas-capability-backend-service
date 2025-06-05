package ru.beeline.capability.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.beeline.capability.domain.*;
import ru.beeline.capability.dto.*;
import ru.beeline.capability.exception.ForbiddenException;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.exception.PackageRegistrationException;
import ru.beeline.capability.exception.ValidationException;
import ru.beeline.capability.repository.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CapabilityMapService {

    @Autowired
    TechCapabilityRelationsRepository techCapabilityRelationsRepository;

    @Autowired
    BusinessCapabilityRepository businessCapabilityRepository;

    @Autowired
    TechCapabilityRepository techCapabilityRepository;

    @Autowired
    CapabilityMapRepository capabilityMapRepository;

    @Autowired
    EntityTypeRepository entityTypeRepository;

    @Autowired
    CriteriaBcRepository criteriaBcRepository;

    @Autowired
    CriteriaTcRepository criteriaTcRepository;
    @Autowired
    TcGroupRepository tcGroupRepository;

    @Autowired
    UserMapRepository userMapRepository;

    @Autowired
    BcGroupRepository bcGroupRepository;

    @Autowired
    GroupRepository groupRepository;


    public void validatePostCapabilityMapDTO(PostCapabilityMapDTO postCapabilityMapDTO) {
        StringBuilder errMsg = new StringBuilder();
        if (postCapabilityMapDTO.getName() == null || postCapabilityMapDTO.getName().equals("")) {
            errMsg.append("Отсутствует обязательное поле name");
        }
        if (postCapabilityMapDTO.getTypeId() == null || postCapabilityMapDTO.getTypeId().equals("")) {
            errMsg.append("Отсутствует обязательное поле TypeId");
        }
        if (!errMsg.toString().isEmpty()) {
            throw new ValidationException(errMsg.toString());
        }
    }

    public void validateUserIdHeaders(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new ForbiddenException("Отсутствует заголовок USER_ID_HEADER");
        }
        try {
            Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            throw new ForbiddenException("USER_ID_HEADER должен быть числом");
        }
    }

    public void findEntityTypeById(PostCapabilityMapDTO postCapabilityMapDTO) {
        Optional<EntityType> entityType = entityTypeRepository.findById(postCapabilityMapDTO.getTypeId().longValue());
        if (entityType.isEmpty()) {
            throw new NotFoundException(String.format("400: Запись c typeId %s не найдена",
                                                      postCapabilityMapDTO.getTypeId()));
        }
    }

    public CreateCapabilityMapResponseDTO createCapabilityMap(PostCapabilityMapDTO postCapabilityMapDTO,
                                                              String userId) {
        validateUserIdHeaders(userId);
        validatePostCapabilityMapDTO(postCapabilityMapDTO);
        findEntityTypeById(postCapabilityMapDTO);
        CapabilityMap capabilityMap = CapabilityMap.builder()
                .name(postCapabilityMapDTO.getName())
                .description(postCapabilityMapDTO.getDescription())
                .typeId(postCapabilityMapDTO.getTypeId())
                .createDate(new Date())
                .build();
        capabilityMap = capabilityMapRepository.save(capabilityMap);
        Integer mapId = capabilityMap.getId();
        UserMap userMap = UserMap.builder()
                .userId(Integer.valueOf(userId))
                .mapId(mapId.intValue())
                .author(true)
                .build();
        userMapRepository.save(userMap);
        return CreateCapabilityMapResponseDTO.builder().id(capabilityMap.getId()).build();
    }

    public List<EntityType> getCapabilityMapTypes(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new ForbiddenException("Отсутствует заголовок USER_ID_HEADER");
        }
        List<EntityType> result = entityTypeRepository.findAll();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Список пуст");
        }
        return result;
    }

    public void patchValidateBody(List<PatchCapabilityMapDTO> patchCapabilityMapDTO) {
        for (PatchCapabilityMapDTO patchCapabilityMap : patchCapabilityMapDTO) {
            boolean hasCapabilityIds = patchCapabilityMap.getCapabilityIds() != null && !patchCapabilityMap.getCapabilityIds()
                    .isEmpty();
            boolean hasChildrenGroups = patchCapabilityMap.getChildrenGroups() != null && !patchCapabilityMap.getChildrenGroups()
                    .isEmpty();
            if (hasCapabilityIds) {
                validateNameGroup(patchCapabilityMap.getNameGroup());
            }
            if (hasChildrenGroups) {
                for (ChildrenGroupDTO childrenGroupDTO : patchCapabilityMap.getChildrenGroups()) {
                    validateNameGroup(childrenGroupDTO.getNameGroup());
                }
            }
        }
    }

    private void validateNameGroup(String nameGroup) {
        if (nameGroup == null || nameGroup.isEmpty()) {
            throw new ValidationException("Отсутствует обязательное поле Name Group");
        }
    }

    private CapabilityMap findCapabilityMapById(Integer id) {
        Optional<CapabilityMap> optionalCapabilityMap = capabilityMapRepository.findById(id);
        CapabilityMap capabilityMap = optionalCapabilityMap.orElseThrow(() -> new NotFoundException(
                "404: Запись в таблице maps не найдена"));
        if (capabilityMap.getDeletedDate() != null) {
            throw new NotFoundException("404: Запись в таблице maps удалена");
        }
        return capabilityMap;
    }

    public void patchCapabilityMap(Integer mapId,
                                   List<PatchCapabilityMapDTO> patchCapabilityMapDTOList,
                                   String userId) {
        validateUserIdHeaders(userId);
        patchValidateBody(patchCapabilityMapDTOList);
        CapabilityMap capabilityMap = findCapabilityMapById(mapId);
        capabilityMap.setUpdateDate(new Date());
        capabilityMapRepository.save(capabilityMap);
        userMapRepository.findByUserIdAndMapIdAndAuthorTrue(Integer.valueOf(userId), mapId)
                .orElseThrow(() -> new NotFoundException("403: Запись User Map не найдена"));
        Optional<EntityType> optionalEntityType = entityTypeRepository.findById(capabilityMap.getTypeId().longValue());
        String entityTypeName = optionalEntityType.get().getName();
        List<Group> groupsList = groupRepository.findAllByMapId(mapId);
        if (!groupsList.isEmpty()) {
            groupRepository.deleteAll(groupsList);
        }
        Integer saveGroupId;
        for (PatchCapabilityMapDTO patchCapabilityMap : patchCapabilityMapDTOList) {
            boolean hasCapabilityIds = patchCapabilityMap.getCapabilityIds() != null && !patchCapabilityMap.getCapabilityIds()
                    .isEmpty();
            boolean hasChildrenGroups = patchCapabilityMap.getCapabilityIds() != null && !patchCapabilityMap.getChildrenGroups()
                    .isEmpty();
            Group group = Group.builder().name(patchCapabilityMap.getNameGroup()).mapId(mapId).build();
            group = groupRepository.save(group);
            saveGroupId = group.getId();
            if (hasCapabilityIds) {
                saveCapabilityGroups(entityTypeName, patchCapabilityMap.getCapabilityIds(), saveGroupId);
            }
            if (hasChildrenGroups) {
                for (ChildrenGroupDTO childrenGroupDTO : patchCapabilityMap.getChildrenGroups()) {
                    Group groupChildrenGroup = Group.builder()
                            .name(childrenGroupDTO.getNameGroup())
                            .mapId(mapId)
                            .parentId(String.valueOf(saveGroupId))
                            .build();
                    groupRepository.save(groupChildrenGroup);
                    Integer saveChildrenGroupId = groupChildrenGroup.getId();
                    saveCapabilityGroups(entityTypeName, childrenGroupDTO.getCapabilityId(), saveChildrenGroupId);
                }
            }
        }
    }

    private void nameValidateBody(NameAndDescriptionDTO nameAndDescriptionDTO) {
        StringBuilder errMsg = new StringBuilder();
        if (nameAndDescriptionDTO.getName() == null || nameAndDescriptionDTO.getName().equals("")) {
            errMsg.append("Отсутствует обязательное поле name");
        }
        if (!errMsg.toString().isEmpty()) {
            throw new ValidationException(errMsg.toString());
        }
    }

    public void patchNameAndDescriptionCapabilityMap(Integer mapId,
                                                     NameAndDescriptionDTO nameAndDescriptionDTO,
                                                     String userId) {
        validateUserIdHeaders(userId);
        nameValidateBody(nameAndDescriptionDTO);
        userMapRepository.findByUserIdAndMapIdAndAuthorTrue(Integer.valueOf(userId), mapId)
                .orElseThrow(() -> new NotFoundException("Запись User Map не найдена"));
        CapabilityMap capabilityMap = findCapabilityMapById(mapId);
        if (!(capabilityMap.getTypeId() == nameAndDescriptionDTO.getType().getId().intValue())) {
            List<Group> groups = groupRepository.findAllByMapId(mapId);
            if (!groups.isEmpty()) {
                throw new PackageRegistrationException("Найдены записи в таблице groups с данным map id");
            }
        }
        capabilityMap.setName(nameAndDescriptionDTO.getName());
        capabilityMap.setDescription(nameAndDescriptionDTO.getDescription());
        if (nameAndDescriptionDTO.getType() != null && nameAndDescriptionDTO.getType().getId() != null) {
            capabilityMap.setTypeId(nameAndDescriptionDTO.getType().getId().intValue());
        }
        capabilityMap.setUpdateDate(new Date());
        capabilityMapRepository.save(capabilityMap);
    }

    private void saveCapabilityGroups(String entityTypeName, List<Integer> capabilityIds, Integer groupId) {
        try {
            switch (entityTypeName) {
                case "BUSINESS_CAPABILITY":
                    for (Integer capabilityId : capabilityIds) {
                        BcGroup bcGroup = BcGroup.builder().bcId(capabilityId).groupId(groupId).build();
                        bcGroupRepository.save(bcGroup);
                    }
                    break;
                case "TECH_CAPABILITY":
                    for (Integer capabilityId : capabilityIds) {
                        TcGroup tcGroup = TcGroup.builder().tcId(capabilityId).groupId(groupId).build();
                        tcGroupRepository.save(tcGroup);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестный тип сущности: " + entityTypeName);
            }
        } catch (DataIntegrityViolationException e) {
            throw new ForbiddenException("Записи с таким Id нет в таблице BC или TC");
        }
    }

    public void deleteCapabilityMap(Integer mapId, String userId) {
        validateUserIdHeaders(userId);
        Optional<UserMap> optionalUserMap = userMapRepository.findByUserIdAndMapId(Integer.valueOf(userId), mapId);
        if (optionalUserMap.isEmpty()) {
            throw new NotFoundException("404: Запись User Map не найдена");
        }
        Optional<CapabilityMap> optionalCapabilityMap = capabilityMapRepository.findById(mapId);
        if (optionalCapabilityMap.isEmpty() || optionalCapabilityMap.get().getDeletedDate() != null) {
            throw new NotFoundException("404: Not Found");
        }
        optionalCapabilityMap.ifPresent(capabilityMap -> {
            capabilityMap.setDeletedDate(new Date());
            capabilityMapRepository.save(capabilityMap);
        });
        List<Group> groupsList = groupRepository.findAllByMapId(mapId);
        if (!groupsList.isEmpty()) {
            groupRepository.deleteAll(groupsList);
        }
    }

    public List<ShortCapabilityMapDTO> getCapabilityMaps(String userId) {
        validateUserIdHeaders(userId);
        List<Integer> mapIds = userMapRepository.findAllByUserIdAndAuthorTrue(Integer.valueOf(userId))
                .stream()
                .map(UserMap::getMapId)
                .collect(Collectors.toList());
        List<CapabilityMap> capabilityMaps = capabilityMapRepository.findAllByIdInAndDeletedDateIsNull(mapIds);
        if (capabilityMaps.isEmpty()) {
            return new ArrayList<>();
        }
        capabilityMaps.sort(Comparator.comparing(CapabilityMap::getCreateDate).reversed());
        return capabilityMaps.stream()
                .map(capabilityMap -> ShortCapabilityMapDTO.builder()
                        .id(capabilityMap.getId())
                        .name(capabilityMap.getName())
                        .description(capabilityMap.getDescription())
                        .createdDate(capabilityMap.getCreateDate())
                        .updatedDate(capabilityMap.getUpdateDate())
                        .type(entityTypeRepository.findById(capabilityMap.getTypeId().longValue()).get())
                        .build())
                .collect(Collectors.toList());
    }

    public GetCapabilityMapByIdDTO getCapabilityMapById(Integer id) {
        Optional<CapabilityMap> optionalCapabilityMap = capabilityMapRepository.findById(id);
        if (optionalCapabilityMap.isEmpty() || optionalCapabilityMap.get().getDeletedDate() != null) {
            throw new NotFoundException("404: Карта не найдена");
        }
        GetCapabilityMapByIdDTO getCapabilityMapByIdDTO = createGetCapabilityMapByIdDTO(optionalCapabilityMap.get());
        List<Group> groupsList = groupRepository.findAllByMapId(id);
        List<Group> groupListParentIdIsNull = getGroupsWithNullParentId(groupsList);
        List<Group> groupListParentIdNotNull = getGroupsWithNotNullParentId(groupsList);
        if (!groupListParentIdIsNull.isEmpty()) {
            getCapabilityMapByIdDTO.setGroups(createGroupDTOList(groupListParentIdIsNull));
            if (!groupListParentIdNotNull.isEmpty()) {
                setChildrenGroups(getCapabilityMapByIdDTO.getGroups(),
                                  createGetChildrenGroupsDTOList(groupListParentIdNotNull));
            }
            List<Integer> groupDTOListParentIdIsNullIDS = getGroupIds(groupListParentIdIsNull);
            List<Integer> groupDTOListParentIdNotNullIDS = getGroupIds(groupListParentIdNotNull);
            if (getCapabilityMapByIdDTO.getType().getId() == 2) {
                createBcGroup(getCapabilityMapByIdDTO.getGroups(),
                              groupDTOListParentIdIsNullIDS,
                              groupDTOListParentIdNotNullIDS);
            } else {
                if (getCapabilityMapByIdDTO.getType().getId() == 1) {
                    createTcGroup(getCapabilityMapByIdDTO.getGroups(),
                                  groupDTOListParentIdIsNullIDS,
                                  groupDTOListParentIdNotNullIDS);
                }
            }
        }
        return getCapabilityMapByIdDTO;
    }

    private List<Integer> getGroupIds(List<Group> groupList) {
        return groupList.stream().map(Group::getId).collect(Collectors.toList());
    }

    private GetCapabilityMapByIdDTO createGetCapabilityMapByIdDTO(CapabilityMap capabilityMap) {
        return GetCapabilityMapByIdDTO.builder()
                .name(capabilityMap.getName())
                .description(capabilityMap.getDescription())
                .type(entityTypeRepository.findById(capabilityMap.getTypeId().longValue()).get())
                .build();
    }

    private List<Group> getGroupsWithNullParentId(List<Group> groupsList) {
        return groupsList.stream().filter(group -> group.getParentId() == null).toList();
    }

    private List<Group> getGroupsWithNotNullParentId(List<Group> groupsList) {
        return groupsList.stream().filter(group -> group.getParentId() != null).toList();
    }

    private List<GroupDTO> createGroupDTOList(List<Group> groupList) {
        return groupList.stream()
                .map(group -> GroupDTO.builder().groupId(group.getId()).nameGroup(group.getName()).build())
                .collect(Collectors.toList());
    }

    private List<GetChildrenGroupsDTO> createGetChildrenGroupsDTOList(List<Group> groupList) {
        return groupList.stream()
                .map(group -> GetChildrenGroupsDTO.builder()
                        .groupId(group.getId())
                        .nameGroup(group.getName())
                        .parentId(Integer.valueOf(group.getParentId()))
                        .build())
                .toList();
    }

    private void setChildrenGroups(List<GroupDTO> groupDTOList, List<GetChildrenGroupsDTO> childrenGroups) {
        for (GroupDTO groupDTO : groupDTOList) {
            List<GetChildrenGroupsDTO> childrenGroup = childrenGroups.stream()
                    .filter(child -> child.getParentId().equals(groupDTO.getGroupId()))
                    .collect(Collectors.toList());
            groupDTO.setChildrenGroup(childrenGroup);
        }
    }

    private void createBcGroup(List<GroupDTO> groups,
                               List<Integer> groupDTOListParentIdIsNullIDS,
                               List<Integer> groupDTOListParentIdNotNullIDS) {
        boolean parent = !groupDTOListParentIdIsNullIDS.isEmpty();
        boolean children = !groupDTOListParentIdNotNullIDS.isEmpty();
        if (parent) {
            List<BcGroup> bcGroupList = bcGroupRepository.findAllByGroupIdIn(groupDTOListParentIdIsNullIDS);
            for (GroupDTO groupDTO : groups) {
                groupDTO.setCapability(new ArrayList<>());
                for (BcGroup bcGroup : bcGroupList) {
                    if (groupDTO.getGroupId().equals(bcGroup.getGroupId())) {
                        Optional<BusinessCapability> businessCapability = businessCapabilityRepository.findById(bcGroup.getBcId()
                                                                                                                        .longValue());
                        businessCapability.ifPresent(capability -> groupDTO.getCapability()
                                .add(buildBcCapabilityDTO(capability)));
                    }
                }
            }
        }

        if (children) {
            List<BcGroup> bcGroups = bcGroupRepository.findAllByGroupIdIn(groupDTOListParentIdNotNullIDS);
            for (GroupDTO groupDTO : groups) {
                for (GetChildrenGroupsDTO childrenGroupDTO : groupDTO.getChildrenGroup()) {
                    childrenGroupDTO.setCapability(new ArrayList<>());
                    for (BcGroup bcGroup : bcGroups) {
                        if (childrenGroupDTO.getGroupId().equals(bcGroup.getGroupId())) {
                            Optional<BusinessCapability> businessCapability = businessCapabilityRepository.findById(
                                    bcGroup.getBcId().longValue());
                            businessCapability.ifPresent(capability -> childrenGroupDTO.getCapability()
                                    .add(buildBcCapabilityDTO(capability)));
                        }
                    }
                }
            }
        }
    }

    private void createTcGroup(List<GroupDTO> groups,
                               List<Integer> groupDTOListParentIdIsNullIDS,
                               List<Integer> groupDTOListParentIdNotNullIDS) {
        boolean parent = !groupDTOListParentIdIsNullIDS.isEmpty();
        boolean children = !groupDTOListParentIdNotNullIDS.isEmpty();
        List<TcGroup> allTcGroups = new ArrayList<>();
        if (parent) {
            allTcGroups.addAll(tcGroupRepository.findAllByGroupIdIn(groupDTOListParentIdIsNullIDS));
        }
        if (children) {
            allTcGroups.addAll(tcGroupRepository.findAllByGroupIdIn(groupDTOListParentIdNotNullIDS));
        }
        Set<Long> tcIds = allTcGroups.stream()
                .map(tcGroup -> tcGroup.getTcId().longValue())
                .collect(Collectors.toSet());
        Map<Long, TechCapability> techCapabilityMap = techCapabilityRepository.findAllById(tcIds)
                .stream()
                .collect(Collectors.toMap(TechCapability::getId, Function.identity()));
        if (parent) {
            groups.forEach(groupDTO -> groupDTO.setCapability(allTcGroups.stream()
                                                                      .filter(tcGroup -> groupDTO.getGroupId()
                                                                              .equals(tcGroup.getGroupId()))
                                                                      .map(tcGroup -> techCapabilityMap.get(tcGroup.getTcId()
                                                                                                                    .longValue()))
                                                                      .filter(Objects::nonNull)
                                                                      .map(this::buildTcCapabilityDTO)
                                                                      .collect(Collectors.toList())));
        }
        if (children) {
            groups.forEach(groupDTO -> groupDTO.getChildrenGroup()
                    .forEach(childrenGroupDTO -> childrenGroupDTO.setCapability(allTcGroups.stream()
                                                                                        .filter(tcGroup -> childrenGroupDTO.getGroupId()
                                                                                                .equals(tcGroup.getGroupId()))
                                                                                        .map(tcGroup -> techCapabilityMap.get(
                                                                                                tcGroup.getTcId()
                                                                                                        .longValue()))
                                                                                        .filter(Objects::nonNull)
                                                                                        .map(this::buildTcCapabilityDTO)
                                                                                        .collect(Collectors.toList()))));
        }
    }

    private CapabilityDTO buildBcCapabilityDTO(BusinessCapability businessCapability) {
        List<CriteriasBc> criteriasBc = criteriaBcRepository.findAllByBcId(businessCapability.getId());
        return CapabilityDTO.builder()
                .id(businessCapability.getId())
                .code(businessCapability.getCode())
                .name(businessCapability.getName())
                .description(businessCapability.getDescription())
                .author(businessCapability.getAuthor())
                .status(businessCapability.getStatus())
                .link(businessCapability.getLink())
                .createdDate(businessCapability.getCreatedDate())
                .isDomain(businessCapability.isDomain())
                .updatedDate(businessCapability.getLastModifiedDate())
                .owner(businessCapability.getOwner())
                .parentId(businessCapability.getParentId() != null ? businessCapability.getParentId().intValue() : 0)
                .criteria(criteriasBc == null || criteriasBc.isEmpty() ? new ArrayList<>() : buildCriteriaBcDTO(
                        criteriasBc))
                .build();
    }

    public List<CriteriaDTO> buildCriteriaBcDTO(List<CriteriasBc> criterias) {
        List<CriteriaDTO> result = new ArrayList<>();
        if (criterias != null && !criterias.isEmpty()) {
            for (CriteriasBc c : criterias) {
                result.add(CriteriaDTO.builder()
                                   .criteriaId(c.getCriterionId().intValue())
                                   .value(c.getValue())
                                   .grade(c.getGrade())
                                   .comment(c.getComment())
                                   .build());
            }
        }
        return result;
    }

    public List<CriteriaDTO> buildCriteriaTcDTO(List<CriteriasTc> criterias) {
        List<CriteriaDTO> result = new ArrayList<>();
        if (criterias != null && !criterias.isEmpty()) {
            for (CriteriasTc c : criterias) {
                result.add(CriteriaDTO.builder()
                                   .criteriaId(c.getCriterionId().intValue())
                                   .value(c.getValue())
                                   .grade(c.getGrade())
                                   .comment(c.getComment())
                                   .build());
            }
        }
        return result;
    }

    private CapabilityDTO buildTcCapabilityDTO(TechCapability techCapability) {
        List<CriteriasTc> criteriasTc = criteriaTcRepository.findAllByTcId(techCapability.getId());
        return CapabilityDTO.builder()
                .id(techCapability.getId())
                .code(techCapability.getCode())
                .name(techCapability.getName())
                .description(techCapability.getDescription())
                .author(techCapability.getAuthor())
                .status(techCapability.getStatus())
                .link(techCapability.getLink())
                .createdDate(techCapability.getCreatedDate())
                .updatedDate(techCapability.getLastModifiedDate())
                .owner(techCapability.getOwner())
                .responsibilityProductId(techCapability.getResponsibilityProductId())
                .criteria(criteriasTc != null && !criteriasTc.isEmpty() ? buildCriteriaTcDTO(criteriasTc) : new ArrayList<>())
                .build();
    }
}
