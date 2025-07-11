package ru.beeline.capability.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.client.AIToolClient;
import ru.beeline.capability.domain.CriteriasTc;
import ru.beeline.capability.domain.Promt;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.repository.CriteriaTcRepository;
import ru.beeline.capability.repository.EnumCriteriaRepository;
import ru.beeline.capability.repository.PromtRepository;
import ru.beeline.capability.repository.TechCapabilityRepository;

@Service
public class QualityService {
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

    public void checkQuality(Long entityId, String changeType, String name) {
        TechCapability techCapability = techCapabilityRepository.findAllByIdAndDeletedDateIsNull(entityId);
        if(techCapability!=null){
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
            JsonNode message = aiConclusion.aiConclusion(jsonBody);
            if(message !=null){
                Long enumCriteriaId = enumCriteriaRepository.findByName("Качество описания TC").getId();
                CriteriasTc criteriasTc = criteriaTcRepository.findByCriterionIdAndTcId(
                        enumCriteriaId,
                        techCapability.getId());
                if(criteriasTc!=null){
                    criteriasTc.setGrade(message.get("rating").asInt());
                    criteriasTc.setValue(message.get("rating").asInt());
                    criteriasTc.setComment(message.get("descr").asText());
                    criteriaTcRepository.save(criteriasTc);
                }else{
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
}
