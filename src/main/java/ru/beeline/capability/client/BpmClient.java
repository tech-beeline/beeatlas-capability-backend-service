package ru.beeline.capability.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.beeline.capability.controller.RequestContext;
import ru.beeline.capability.dto.CommentDTO;
import ru.beeline.fdmlib.dto.bpm.ApplicationExtendedDTO;

import java.util.HashMap;
import java.util.Map;

import static ru.beeline.capability.utils.Constants.*;

@Slf4j
@Service
public class BpmClient {
    private final RestTemplate restTemplate;

    @Value("${integration.bpm-server-url}")
    private final String bpmBaseUrl;

    public BpmClient(@Value("${integration.bpm-server-url}") String bpmBaseUrl, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.bpmBaseUrl = bpmBaseUrl;
    }

    public void editStatusProcess(String comment, String businessKey, String statusAlias) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(USER_ID_HEADER, RequestContext.getUserId());
            headers.set(USER_PERMISSION_HEADER, RequestContext.getUserPermissions().toString());
            headers.set(USER_PRODUCTS_IDS_HEADER, RequestContext.getUserProducts().toString());
            headers.set(USER_ROLES_HEADER, RequestContext.getRoles().toString());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CommentDTO> entity = new HttpEntity<>(CommentDTO.builder().comment(comment).build(), headers);

            ResponseEntity response = restTemplate.exchange(bpmBaseUrl + "/application/" + businessKey + "/change-status/" + statusAlias,
                                                            HttpMethod.PATCH,
                                                            entity,
                                                            ResponseEntity.class).getBody();
            if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
                throw new RuntimeException("editStatusProcess filed");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Error while uploading file");
        }
    }

    public void startProcess(String businessKey, Map<String, Object> variables) {
        String url = bpmBaseUrl + "/engine-rest/process-definition/key/Process_11grtql/start?async=true";

        Map<String, Object> body = new HashMap<>();
        body.put("businessKey", businessKey);

        Map<String, Object> vars = new HashMap<>();
        variables.forEach((k, v) -> {
            Map<String, Object> varMap = new HashMap<>();
            varMap.put("value", v);
            if (v instanceof Integer) {
                varMap.put("type", "Integer");
            } else if (v instanceof String) {
                varMap.put("type", "String");
            } else {
                varMap.put("type", "Object");
            }
            vars.put(k, varMap);
        });

        body.put("variables", vars);

        restTemplate.postForEntity(url, body, Void.class);
    }

    public ApplicationExtendedDTO getApplication(String businessKey) {
        try {
            HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(USER_ID_HEADER, RequestContext.getUserId());
        headers.set(USER_PERMISSION_HEADER, RequestContext.getUserPermissions().toString());
        headers.set(USER_PRODUCTS_IDS_HEADER, RequestContext.getUserProducts().toString());
        headers.set(USER_ROLES_HEADER, RequestContext.getRoles().toString());

        log.info("request to bpm");
        ResponseEntity<ApplicationExtendedDTO> response = restTemplate.exchange(bpmBaseUrl + "/camunda-process/api/v1" +
                                                                                        "/application/" + businessKey,
                                                                                HttpMethod.GET,
                                                                                new HttpEntity<>(headers),
                                                                                new ParameterizedTypeReference<ApplicationExtendedDTO>() {});
        log.info("response from bpm: " + response.getBody());
        return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            String msg = "Запись с данным businessKey: " + businessKey + " не найдена";
            log.warn(msg);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg, e);
        }
    }
}
