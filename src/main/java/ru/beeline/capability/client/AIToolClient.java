/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.beeline.capability.controller.RequestContext;
import ru.beeline.capability.dto.aitooldto.AiRequestDTO;
import ru.beeline.capability.exception.ResponseException;

@Slf4j
@Service
public class AIToolClient {
    RestTemplate restTemplate;
    private final String aiToolServerUrl;
    private static final ObjectMapper mapper = new ObjectMapper();

    public AIToolClient(@Value("${integration.ai-tool-server-url}") String aiToolServerUrl, RestTemplate restTemplate) {
        this.aiToolServerUrl = aiToolServerUrl;
        this.restTemplate = restTemplate;
    }

    public JsonNode aiConclusion(String jsonBody) {
        int count = 0;
        JsonNode result = null;
        while (count < 3) {
            result = request(jsonBody);

            if (result != null) {
                break;
            }
            count++;
        }
        return result;
    }

    public JsonNode request(String jsonBody) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(aiToolServerUrl,
                    HttpMethod.POST,
                    entity,
                    String.class);

            String responseBody = response.getBody();
            if (responseBody != null) {
                return cleanAndValidateJson(extractContentFromResponse(responseBody));
            }
        } catch (Exception e) {
            log.error("Ошибка в aiConclusion: " + e.getMessage());
        }
        return null;
    }

    public String extractContentFromResponse(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");

            if (!contentNode.isMissingNode()) {
                return contentNode.asText();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static JsonNode cleanAndValidateJson(String rawContent) {
        if (rawContent == null) {
            return null;
        }

        String cleaned = rawContent.replace("\\n", "")
                .replace("```json", "")
                .replace("```", "")
                .replace("\\r", "")
                .replace("\\t", "")
                .replace("\\\"", "\"")

                .trim();

        try {
            JsonNode jsonNode = mapper.readTree(cleaned);

            if (jsonNode.has("rating") && jsonNode.has("descr")) {
                return jsonNode;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public String postAiToolBeeline(AiRequestDTO aiRequestDTO) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<AiRequestDTO> entity = new HttpEntity<>(aiRequestDTO, headers);

            return restTemplate.exchange(aiToolServerUrl,
                    HttpMethod.POST, entity, String.class).getBody();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public String postAiTool(AiRequestDTO aiRequestDTO) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + RequestContext.getOpenaiToken());

            HttpEntity<AiRequestDTO> entity = new HttpEntity<>(aiRequestDTO, headers);
            log.info(RequestContext.getOpenaiHost() + "/api/v1/chat/completions");
            return restTemplate.exchange(RequestContext.getOpenaiHost() + "/v1/chat/completions",
                    HttpMethod.POST, entity, String.class).getBody();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ResponseException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при запросе в сторонний сервис: " + e.getMessage());
        }
    }
}
