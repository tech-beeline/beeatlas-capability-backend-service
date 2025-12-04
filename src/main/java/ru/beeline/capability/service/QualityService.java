package ru.beeline.capability.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.client.AIToolClient;
import ru.beeline.capability.client.ProductClient;
import ru.beeline.capability.domain.CriteriasTc;
import ru.beeline.capability.domain.Promt;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.dto.ProductAvailableDTO;
import ru.beeline.capability.repository.CriteriaTcRepository;
import ru.beeline.capability.repository.EnumCriteriaRepository;
import ru.beeline.capability.repository.PromtRepository;
import ru.beeline.capability.repository.TechCapabilityRepository;

@Slf4j
@Service
public class QualityService {

    @Autowired
    private ProductClient productClient;
    @Autowired
    private TechCapabilityRepository techCapabilityRepository;
    @Autowired
    private PromtRepository promtRepository;
    @Autowired
    private EnumCriteriaRepository enumCriteriaRepository;
    @Autowired
    private CriteriaTcRepository criteriaTcRepository;
    @Autowired
    private AIToolClient aiConclusion;

    public void checkQuality(Long id) {
        TechCapability techCapability = techCapabilityRepository.findAllByIdAndDeletedDateIsNull(id);
        if (techCapability != null) {
            Promt promt = promtRepository.findByAlias("tc_quality_description");
            String content = promt.getPromt().replace("<!!!>", techCapability.getDescription());
            String jsonBody = "{\n" +
                    "  \"messages\": [\n" +
                    "    {\n" +
                    "      \"role\": \"user\",\n" +
                    "      \"content\": \"" + escapeJson(content) + "\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"model\": \"" + promt.getModel() + "\",\n" +
                    "  \"stream\": false,\n" +
                    "  \"add_generation_prompt\": false\n" +
                    "}";
            log.info("call AITool");
            JsonNode message = aiConclusion.aiConclusion(jsonBody);
            if (message != null) {
                Long enumCriteriaId = enumCriteriaRepository.findByName("Качество описания TC").getId();
                CriteriasTc criteriasTc = criteriaTcRepository.findByCriterionIdAndTcId(enumCriteriaId,
                        techCapability.getId());
                if (criteriasTc != null) {
                    criteriasTc.setGrade(message.get("rating").asInt());
                    criteriasTc.setValue(message.get("rating").asInt());
                    criteriasTc.setComment(message.get("descr").asText());
                    criteriaTcRepository.save(criteriasTc);
                } else {
                    criteriaTcRepository.save(CriteriasTc.builder()
                            .criterionId(enumCriteriaId)
                            .tcId(techCapability.getId())
                            .grade(message.get("rating").asInt())
                            .value(message.get("rating").asInt())
                            .comment(message.get("descr").asText())
                            .build());
                }
            }
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    public void calculationCriterias(JsonNode jsonNode) {
        String changeType = jsonNode.get("changeType").asText();
        String id = jsonNode.get("entityId").asText();
        ProductAvailableDTO available = productClient.getAvailability(id);


    }
}
