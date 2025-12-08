package ru.beeline.capability.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.beeline.capability.service.BusinessCapabilityService;

@Slf4j
@Component
@EnableRabbit
public class SparxConsumer {

    @Autowired
    private BusinessCapabilityService businessCapabilityService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "${queue.bc-sparx.name}")
    public void techQueue(String message) {
        log.info("Получено сообщение из bc-sparx queue: " + message);
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(message);
            if (!isValidMessage(jsonNode)) {
                log.error("Сообщение не соответствует требуемому формату: " + message);
            }else {
                businessCapabilityService.processMessage(jsonNode);
            }
        } catch (Exception e) {
            log.error("Ошибка валидации сообщения: " + e.getMessage());
        }
    }

    private boolean isValidMessage(JsonNode jsonNode) {
        return jsonNode.has("name") &&
                jsonNode.has("id") &&
                jsonNode.has("changeType") &&
                jsonNode.has("source");
    }
}

