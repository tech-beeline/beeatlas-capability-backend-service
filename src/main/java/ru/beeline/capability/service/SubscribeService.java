package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.EntityType.EntityType;
import ru.beeline.capability.cleint.NotificationClient;
import ru.beeline.capability.controller.RequestContext;
import ru.beeline.capability.dto.CapabilitySubscribedDTO;
import ru.beeline.capability.mapper.BusinessCapabilityMapper;
import ru.beeline.capability.mapper.SubscribeCapabilityMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubscribeService {
    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private TechCapabilityService techCapabilityService;

    @Autowired
    private BusinessCapabilityService businessCapabilityService;

    @Autowired
    private SubscribeCapabilityMapper subscribeCapabilityMapper;

    @Autowired
    private BusinessCapabilityMapper businessCapabilityMapper;

    public List<CapabilitySubscribedDTO> getCapabilitiesSubscribed(EntityType entityType) {
        List<Long> subscribes = notificationClient.getSubscribes(entityType);
        if (subscribes.isEmpty()) {
            return new ArrayList<>();
        }
        if (EntityType.TECH_CAPABILITY.equals(entityType)) {
            return subscribeCapabilityMapper.convert(techCapabilityService.getByIdIn(subscribes));
        } else {
            return businessCapabilityMapper.convertToCapabilitySubscribedDTOs(businessCapabilityService.getByIdIn(subscribes));
        }
    }
}
