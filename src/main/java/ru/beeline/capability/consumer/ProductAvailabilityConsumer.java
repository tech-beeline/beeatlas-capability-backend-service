/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.beeline.capability.service.QualityService;

@Slf4j
@Component
@EnableRabbit
public class ProductAvailabilityConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    QualityService qualityService;

    @RabbitListener(queues = "${queue.product-availability.name}")
    public void productQueue(String message) {
        log.info("Получено сообщение из product_availability queue: " + message);
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(message);
            if (!isValidMessage(jsonNode)) {
                log.error("Сообщение не соответствует требуемому формату: " + message);
            } else {
                qualityService.calculationCriterias(jsonNode);
            }
        } catch (Exception e) {
            log.error("Ошибка обработки сообщения: " + e.getMessage());
        }
    }

    private boolean isValidMessage(JsonNode jsonNode) {
        return jsonNode.has("entityId") &&
                jsonNode.has("changeType");
    }
}
