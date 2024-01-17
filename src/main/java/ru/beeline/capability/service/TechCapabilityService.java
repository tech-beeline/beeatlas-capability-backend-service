package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.dto.TechCapabilityDTO;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.TechCapabilityRepository;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TechCapabilityService {

    @Autowired
    private TechCapabilityRepository techCapabilityRepository;

    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;

    public List<TechCapabilityDTO> getCapabilities(Integer limit, Integer offset) {
        if(offset == null) {
            offset = 0;
        }
        Pageable pageable = new OffsetBasedPageRequest(offset, limit == null ? Integer.MAX_VALUE : limit);
        Page<TechCapability> techCapabilities = techCapabilityRepository.findCapabilities(pageable);
        Map<TechCapability, List<BusinessCapability>> techCapabilitiesWithParentsMap = new HashMap<>();
        for(TechCapability techCapability : techCapabilities.toList()) {
            List<BusinessCapability> parents = businessCapabilityRepository.findParents(techCapability.getId());
            techCapabilitiesWithParentsMap.put(techCapability, parents);
        }

        return TechCapabilityDTO.convert(techCapabilitiesWithParentsMap);
    }
}
