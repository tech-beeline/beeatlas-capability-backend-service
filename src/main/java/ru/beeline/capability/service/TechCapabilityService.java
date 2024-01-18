package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.dto.TechCapabilityDTO;
import ru.beeline.capability.helper.pagination.OffsetBasedPageRequest;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.TechCapabilityRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

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
        Pageable pageable = new OffsetBasedPageRequest(offset, limit == null || limit == 0 ? Integer.MAX_VALUE : limit, Sort.by(Sort.Direction.ASC, "name"));
        Page<TechCapability> techCapabilities = techCapabilityRepository.findCapabilities(pageable);
        return TechCapabilityDTO.convert(techCapabilities.toList());
    }
}
