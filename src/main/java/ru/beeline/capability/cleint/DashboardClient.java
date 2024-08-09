package ru.beeline.capability.cleint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.beeline.fdmlib.dto.capability.PutBusinessCapabilityDTO;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class DashboardClient {

    RestTemplate restTemplate;
    private final String dashboardServerUrl;

    public DashboardClient(@Value("${integration.dashboard-server-url}") String dashboardServerUrl,
                           RestTemplate restTemplate) {
        this.dashboardServerUrl = dashboardServerUrl;
        this.restTemplate = restTemplate;
    }


    public String putCapability(PutBusinessCapabilityDTO capabilityDTO) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("isDomain", capabilityDTO.getIsDomain());
            requestBody.put("name", capabilityDTO.getName());
            requestBody.put("description", capabilityDTO.getDescription());
            requestBody.put("parent", capabilityDTO.getParent());
            requestBody.put("owner", capabilityDTO.getOwner());
            requestBody.put("author", capabilityDTO.getAuthor());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            return restTemplate.exchange(dashboardServerUrl + "/api/capabilities/" + capabilityDTO.getCode(),
                    HttpMethod.PUT, entity, String.class).getBody();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
