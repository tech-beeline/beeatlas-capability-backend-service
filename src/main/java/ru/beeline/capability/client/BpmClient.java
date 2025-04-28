package ru.beeline.capability.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class BpmClient {
    RestTemplate restTemplate;

    @Value("${integration.bpm-server-url}")
    private String bpmBaseUrl;

    public void startProcess(String processKey, String businessKey, Map<String, Object> variables) {
        String url = bpmBaseUrl + "/process-definition/key/" + processKey + "/start?async=true";

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
}
