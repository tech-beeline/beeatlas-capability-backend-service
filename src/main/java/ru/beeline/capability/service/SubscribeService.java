package ru.beeline.capability.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.EntityType.EntityType;
import ru.beeline.capability.client.NotificationClient;
import ru.beeline.capability.dto.CapabilitySubscribedDTO;
import ru.beeline.capability.mapper.SubscribeCapabilityMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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


    public List<CapabilitySubscribedDTO> getCapabilitiesSubscribed(EntityType entityType) {
        log.info("get subscribes method start");
        List<Long> subscribes = notificationClient.getSubscribes(entityType);
        log.info("the list has been received from notificationClient");
        if (subscribes.isEmpty()) {
            log.info("subscribes are empty");
            return new ArrayList<>();
        }
        if (EntityType.TECH_CAPABILITY.equals(entityType)) {
            return subscribeCapabilityMapper.convert(techCapabilityService.getByIdIn(subscribes));
        } else {
            log.info("convert subscribes To CapabilitySubscribedDTOs");
            List<CapabilitySubscribedDTO> result = subscribeCapabilityMapper.convertToCapabilitySubscribedDTOs(businessCapabilityService.getByIdIn(subscribes));
            log.info("CapabilitySubscribedDTO: " + result.toString());
            return result;
        }
    }
}
