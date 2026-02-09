/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.client.AIToolClient;
import ru.beeline.capability.controller.RequestContext;
import ru.beeline.capability.domain.Promt;
import ru.beeline.capability.dto.PostPromtDTO;
import ru.beeline.capability.dto.PromtDTO;
import ru.beeline.capability.dto.aitooldto.AiRequestDTO;
import ru.beeline.capability.dto.aitooldto.MessageDTO;
import ru.beeline.capability.dto.aitooldto.ResultDTO;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.repository.PromtRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PromtService {

    @Autowired
    private AIToolClient aiToolClient;

    @Autowired
    private PromtRepository promtRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public PromtDTO getPromtByAlias(String alias) {
        Promt promt = findPromtByAlias(alias);
        return PromtDTO.builder()
                .id(promt.getId())
                .model(promt.getModel())
                .alias(promt.getAlias())
                .promt(promt.getPromt())
                .build();
    }

    private Promt findPromtByAlias(String alias) {
        Promt promt = promtRepository.findByAlias(alias);
        if (promt == null) {
            throw new NotFoundException("Промт с данным alias не найден.");
        }
        return promt;
    }

    public ResultDTO postPromtProxy(PostPromtDTO postPromtDTO) {
        validatePromtDTO(postPromtDTO);
        Promt promt = findPromtByAlias(postPromtDTO.getPromtAlias());
        String promtString = promt.getPromt();
        String replacedPromt = promtString.replace("!!!", postPromtDTO.getPromtTarget());
        List<String> aiHeaders = new ArrayList<>();
        aiHeaders.add(RequestContext.getOpenaiHost());
        aiHeaders.add(RequestContext.getOpenaiModel());
        aiHeaders.add(RequestContext.getOpenaiToken());
        boolean aiBeeline = false;
        for (String header : aiHeaders) {
            if (header == null || header.isEmpty()) {
                aiBeeline = true;
                break;
            }
        }
        if (aiBeeline) {
            return ResultDTO.builder()
                    .result(extractContentFromJson(aiToolClient
                            .postAiToolBeeline(aiRequestBuilder(promt, replacedPromt, aiBeeline))))
                    .build();
        } else {
            return ResultDTO.builder()
                    .result(extractContentFromJson(aiToolClient
                            .postAiTool(aiRequestBuilder(promt, replacedPromt, aiBeeline))))
                    .build();
        }
    }

    private String extractContentFromJson(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            return root.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

    private AiRequestDTO aiRequestBuilder(Promt promt, String replacedPromt, boolean aiBeeline) {
        String model;
        if (aiBeeline) {
            model = promt.getModel();
        } else {
            model = RequestContext.getOpenaiModel();
        }
        return AiRequestDTO.builder()
                .messages(List.of(MessageDTO.builder().role("user")
                        .content(replacedPromt)
                        .build()))
                .model(model)
                .stream(false)
                .build();
    }

    private void validatePromtDTO(PostPromtDTO postPromtDTO) {
        if (postPromtDTO == null || postPromtDTO.getPromtAlias() == null
                || postPromtDTO.getPromtTarget() == null) {
            throw new IllegalArgumentException("Поля  PromtAlias и PromtTarget обязательные.");
        }

    }
}
