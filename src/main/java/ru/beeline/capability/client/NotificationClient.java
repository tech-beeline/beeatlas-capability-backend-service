/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.beeline.capability.EntityType.EntityType;

import java.util.ArrayList;
import java.util.List;

import static ru.beeline.capability.utils.Constants.USER_ID_HEADER;

@Slf4j
@Service
public class NotificationClient {
    RestTemplate restTemplate;
    private final String notificationServerUrl;

    public NotificationClient(@Value("${integration.notification-server-url}") String notificationServerUrl,
                              RestTemplate restTemplate) {
        this.notificationServerUrl = notificationServerUrl;
        this.restTemplate = restTemplate;
    }

    public List<Long> getSubscribes(EntityType entityType, String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(USER_ID_HEADER, userId);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = notificationServerUrl + "/api/v1/subscribe/" + entityType.name();

            log.info("request to notificationServerUrl with entityType= " + entityType.name());
            ResponseEntity<List<Long>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Long>>() {
                    });
            log.info("response from notificationServerUrl: " + response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error occurred while trying to get all entity subscriptions: ", e);
            return new ArrayList<>();
        }
    }
}
