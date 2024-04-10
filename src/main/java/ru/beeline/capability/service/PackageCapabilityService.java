package ru.beeline.capability.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.beeline.capability.dto.PostTechCapabilityDTO;
import ru.beeline.capability.dto.RegisteredCapabilityPackageDTO;

import java.util.List;

import static ru.beeline.capability.utils.Constants.UPDATE_TECH_CAPABILITIES_OPERATION;
import static ru.beeline.capability.utils.RestHelper.getRestTemplate;

@Slf4j
@Service
public class PackageCapabilityService {

    private final String packLoaderServerUrl;

    public PackageCapabilityService(@Value("${integration.pack-loader-server-url}") String packLoaderServerUrl) {
        this.packLoaderServerUrl = packLoaderServerUrl;
    }

    public RegisteredCapabilityPackageDTO registerTechCapabilitiesPackage(List<PostTechCapabilityDTO> techCapabilities) {
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            MultiValueMap<String, String> bodyParamMap = new LinkedMultiValueMap<>();
            bodyParamMap.add("operation", UPDATE_TECH_CAPABILITIES_OPERATION);
            bodyParamMap.add("count", String.valueOf(techCapabilities.size()));

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(bodyParamMap, headers);
            final RestTemplate restTemplate = getRestTemplate();

            return restTemplate.exchange(packLoaderServerUrl + "/api/v1/package",
                    HttpMethod.POST, entity, RegisteredCapabilityPackageDTO.class).getBody();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
