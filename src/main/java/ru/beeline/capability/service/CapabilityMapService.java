package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.beeline.capability.domain.BcGroup;
import ru.beeline.capability.domain.CapabilityMap;
import ru.beeline.capability.domain.EntityType;
import ru.beeline.capability.domain.Group;
import ru.beeline.capability.domain.TcGroup;
import ru.beeline.capability.domain.UserMap;
import ru.beeline.capability.dto.ChildrenGroupDTO;
import ru.beeline.capability.dto.ShortCapabilityMapDTO;
import ru.beeline.capability.dto.PatchCapabilityMapDTO;
import ru.beeline.capability.dto.PostCapabilityMapDTO;
import ru.beeline.capability.exception.ForbiddenException;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.exception.ValidationException;
import ru.beeline.capability.repository.BcGroupRepository;
import ru.beeline.capability.repository.CapabilityMapRepository;
import ru.beeline.capability.repository.EntityTypeRepository;
import ru.beeline.capability.repository.GroupRepository;
import ru.beeline.capability.repository.TcGroupRepository;
import ru.beeline.capability.repository.UserMapRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CapabilityMapService {

    @Autowired
    CapabilityMapRepository capabilityMapRepository;

    @Autowired
    EntityTypeRepository entityTypeRepository;

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
            throw new NotFoundException(String.format("400: Запись c typeId %s не найдена", postCapabilityMapDTO.getTypeId()));
        }
    }

    public void createCapabilityMap(PostCapabilityMapDTO postCapabilityMapDTO, String userId) {
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
            boolean hasCapabilityIds = patchCapabilityMap.getCapabilityIds() != null && !patchCapabilityMap.getCapabilityIds().isEmpty();
            boolean hasChildrenGroups = patchCapabilityMap.getChildrenGroups() != null && !patchCapabilityMap.getChildrenGroups().isEmpty();
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
        CapabilityMap capabilityMap = optionalCapabilityMap.orElseThrow(() ->
                new NotFoundException("404: Запись в таблице maps не найдена"));
        if (capabilityMap.getDeletedDate() != null) {
            throw new NotFoundException("404: Запись в таблице maps удалена");
        }
        return capabilityMap;
    }

    public void patchCapabilityMap(Integer mapId, List<PatchCapabilityMapDTO> patchCapabilityMapDTOList, String userId) {
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
            boolean hasCapabilityIds = patchCapabilityMap.getCapabilityIds() != null && !patchCapabilityMap.getCapabilityIds().isEmpty();
            boolean hasChildrenGroups = patchCapabilityMap.getCapabilityIds() != null && !patchCapabilityMap.getChildrenGroups().isEmpty();
            Group group = Group.builder()
                    .name(patchCapabilityMap.getNameGroup())
                    .mapId(mapId)
                    .build();
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

    private void saveCapabilityGroups(String entityTypeName, List<Integer> capabilityIds, Integer groupId) {
        try {
            switch (entityTypeName) {
                case "BUSINESS_CAPABILITY":
                    for (Integer capabilityId : capabilityIds) {
                        BcGroup bcGroup = BcGroup.builder()
                                .bcId(capabilityId)
                                .groupId(groupId)

                                .build();
                        bcGroupRepository.save(bcGroup);
                    }
                    break;
                case "TECH_CAPABILITY":
                    for (Integer capabilityId : capabilityIds) {
                        TcGroup tcGroup = TcGroup.builder()
                                .tcId(capabilityId)
                                .groupId(groupId)
                                .build();
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
        Optional<CapabilityMap> optionalCapabilityMap = capabilityMapRepository.findById(Integer.valueOf(userId));
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
                        .typeId(capabilityMap.getTypeId())
                        .build())
                .collect(Collectors.toList());
    }
}
