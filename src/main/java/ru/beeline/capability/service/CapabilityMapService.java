package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.domain.CapabilityMap;
import ru.beeline.capability.domain.EntityType;
import ru.beeline.capability.domain.UserMap;
import ru.beeline.capability.dto.PostCapabilityMapDTO;
import ru.beeline.capability.exception.ForbiddenException;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.exception.ValidationException;
import ru.beeline.capability.repository.CapabilityMapRepository;
import ru.beeline.capability.repository.EntityTypeRepository;
import ru.beeline.capability.repository.UserMapRepository;

import java.util.Date;
import java.util.Optional;

@Service
public class CapabilityMapService {

    @Autowired
    CapabilityMapRepository capabilityMapRepository;

    @Autowired
    UserMapRepository userMapRepository;

    @Autowired
    EntityTypeRepository entityTypeRepository;

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

    public void findEntityTypeById(PostCapabilityMapDTO postCapabilityMapDTO) {
        Optional<EntityType> entityType = entityTypeRepository.findById(postCapabilityMapDTO.getTypeId().longValue());
        if (entityType.isEmpty()) {
            throw new NotFoundException((String.format("400: Запись c typeId %s не найдена", postCapabilityMapDTO.getTypeId())));
        }
    }

    public void createCapabilityMap(PostCapabilityMapDTO postCapabilityMapDTO, String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new ForbiddenException("Отсутствует заголовок USER_ID_HEADER");
        }
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
}
