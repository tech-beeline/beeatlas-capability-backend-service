package ru.beeline.capability.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.client.AIToolClient;
import ru.beeline.capability.client.ProductClient;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.CriteriasBc;
import ru.beeline.capability.domain.CriteriasTc;
import ru.beeline.capability.domain.Promt;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.CriteriaBcRepository;
import ru.beeline.capability.repository.CriteriaTcRepository;
import ru.beeline.capability.repository.EnumCriteriaRepository;
import ru.beeline.capability.repository.PromtRepository;
import ru.beeline.capability.repository.TechCapabilityRelationsRepository;
import ru.beeline.capability.repository.TechCapabilityRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QualityService {

    @Autowired
    private ProductClient productClient;
    @Autowired
    private TechCapabilityRepository techCapabilityRepository;
    @Autowired
    private TechCapabilityRelationsRepository techCapabilityRelationsRepository;
    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;
    @Autowired
    private PromtRepository promtRepository;
    @Autowired
    private EnumCriteriaRepository enumCriteriaRepository;
    @Autowired
    private CriteriaTcRepository criteriaTcRepository;
    @Autowired
    private CriteriaBcRepository criteriaBcRepository;
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
        Boolean available = productClient.getAvailability(id).getAvailability();
        log.info("Boolean available: {}", available);
        Long criterionTcId = enumCriteriaRepository.findByName("Доступность ТС").getId();
        Integer criterionBcId = enumCriteriaRepository.findByName("Кол-во недоступных ТС").getId().intValue();
        List<Integer> tcImplementationIds = productClient.getTcImplementationIds(Integer.parseInt(id));
        log.info("Список tcImplementationIds: {}", tcImplementationIds.isEmpty() ? "Пуст" : tcImplementationIds.toString());
        tcImplementationProcess(tcImplementationIds, available, criterionTcId);
        List<TechCapabilityRelations> techCapabilityRelations =
                techCapabilityRelationsRepository.findByTechCapability_IdIn(tcImplementationIds.stream().map(Long::valueOf).toList());
        Set<BusinessCapability> businessCapabilities = techCapabilityRelations.stream()
                .map(TechCapabilityRelations::getBusinessCapability)
                .collect(Collectors.toSet());
        log.info("Количество BusinessCapability: {}", businessCapabilities.size());
        for (BusinessCapability bc : businessCapabilities) {
            Integer bcId = bc.getId().intValue();
            calculateBcCriteriaUp(bcId, criterionBcId, criterionTcId);
        }
        log.info("Метод по добавлению критериев завершен.");
    }

    private void tcImplementationProcess(List<Integer> tcImplementationIds, Boolean available, Long criterionTcId) {
        if (tcImplementationIds.isEmpty()) {
            log.info("Список идентификаторов реализованных TC по продукту пуст");
        }
        for (Integer tcImplementation : tcImplementationIds) {
            CriteriasTc criteriasTc =
                    criteriaTcRepository.findByCriterionIdAndTcId(criterionTcId, tcImplementation.longValue());
            if (criteriasTc != null) {
                log.info("обновление criteriasTc с id: {}", criteriasTc.getId());
                criteriasTc.setValue(available ? 0 : 1);
                criteriasTc.setGrade(available ? 0 : 1);
                criteriaTcRepository.save(criteriasTc);
            } else {
                log.info("Созданеи criteriasTc с criterionId: {}, tcId: {}", criterionTcId, tcImplementation);
                criteriaTcRepository.save(CriteriasTc.builder()
                        .criterionId(criterionTcId)
                        .tcId(tcImplementation.longValue())
                        .value(available ? 0 : 1)
                        .grade(available ? 0 : 1)
                        .build());
            }
        }
    }

    private void calculateBcCriteriaUp(Integer bcId, Integer criterionBcId, Long criterionTcId) {
        Integer currentBcId = bcId;
        while (currentBcId != null) {
            log.info("Расчет критериев BC c parentId: {}", bcId);
            Optional<BusinessCapability> currentOpt =
                    businessCapabilityRepository.findByIdWithChildrenAndTech(currentBcId.longValue());
            if (currentOpt.isEmpty()) {
                log.info("BC с id={} не найден, остановка расчёта вверх", currentBcId);
                break;
            }
            BusinessCapability current = currentOpt.get();
            List<TechCapability> tcChildren = current.getChildren().stream()
                    .map(TechCapabilityRelations::getTechCapability).toList();
            List<Long> tcIds = tcChildren.stream().map(TechCapability::getId).toList();
            log.info("Список tcChildren: {}", tcIds.isEmpty() ? "Пуст" : tcIds.toString());
            int unavailableCount = 0;
            if (!tcIds.isEmpty()) {
                unavailableCount = criteriaTcRepository
                        .findByCriterionIdAndTcIdInAndValue(criterionTcId, tcIds, 1).size();
            }
            List<BusinessCapability> bcChildren = businessCapabilityRepository.findAllByParentId(currentBcId.longValue());
            int sumBcValues = 0;
            log.info("Start value = 0");
            for (BusinessCapability child : bcChildren) {
                Optional<CriteriasBc> crit = criteriaBcRepository.findByBcIdAndCriterionId(child.getId(), criterionBcId.longValue());
                if (crit.isPresent() && crit.get().getValue() != null) {
                    log.info("Найдена CriteriasBc с BcId: {} и CriterionId: {}", child.getId(), criterionBcId);
                    sumBcValues += crit.get().getValue();
                    log.info("value = {}", sumBcValues);
                } else {
                    log.info("Не найдена CriteriasBc с BcId: {} и CriterionId: {}", child.getId(), criterionBcId);
                }
            }
            int finalValue = sumBcValues + unavailableCount;
            log.info("finalValue: {}", finalValue);
            createUpdateCriteriasBc(currentBcId, criterionBcId, finalValue);
            if (current.getParentId() == null) {
                log.info("У BusinessCapability с id: {} ParentId() == null ", current.getId());
                break;
            }
            currentBcId = current.getParentId().intValue();
        }
    }

    private void createUpdateCriteriasBc(Integer currentBcId, Integer criterionBcId, int finalValue) {
        log.info("Создание, обновление CriteriasBc");
        Optional<CriteriasBc> existing = criteriaBcRepository.findByBcIdAndCriterionId(currentBcId.longValue()
                , criterionBcId.longValue());
        if (existing.isPresent()) {
            log.info("Обновление CriteriasBc с id: {} ", existing.get().getId());
            CriteriasBc bcCrit = existing.get();
            bcCrit.setValue(finalValue);
            bcCrit.setGrade(finalValue == 0 ? 0 : 1);
            criteriaBcRepository.save(bcCrit);
        } else {
            log.info("Создание CriteriasBc с criterionId: {}, bcId: {} ", criterionBcId, currentBcId);
            criteriaBcRepository.save(
                    CriteriasBc.builder()
                            .criterionId(criterionBcId.longValue())
                            .bcId(currentBcId.longValue())
                            .value(finalValue)
                            .grade(finalValue == 0 ? 0 : 1)
                            .comment(null)
                            .build()
            );
        }
    }
}
