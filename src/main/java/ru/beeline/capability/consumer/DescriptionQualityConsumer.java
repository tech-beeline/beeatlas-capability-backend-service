package ru.beeline.capability.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.beeline.capability.service.QualityService;


@Slf4j
@Component
@EnableRabbit
public class DescriptionQualityConsumer {

    @Value("${rabbit.delay}")
    private final Integer delayConsumer;

    @Autowired
    QualityService qualityService;
    private ObjectMapper objectMapper = new ObjectMapper();

    public DescriptionQualityConsumer(int delayConsumer) {this.delayConsumer = delayConsumer;}

    @RabbitListener(queues = "${queue.tc-description-quality.name}")
    public void techQueue(String message) {
        log.info("Received message from tc-description-quality queue: " + message);
        JsonNode jsonNode;
        try {
            Thread.sleep(delayConsumer);
            jsonNode = objectMapper.readTree(message);
            if (!jsonNode.has("changeType") || !jsonNode.has("entityId")) {
                log.error("Message does not match the required format: " + message);
                throw new IllegalArgumentException("Message does not match the required format: " + message);
            }
        } catch (Exception e) {
            log.error("Failed to parse message: " + e.getMessage());
            return;
        }

        String changeType = jsonNode.get("changeType").asText();
        if(!changeType.equals("DELETE")) {
            Long entityId = jsonNode.get("entityId").asLong();
            String name = jsonNode.has("name") ? jsonNode.get("name").asText() : null;

            qualityService.checkQuality(entityId);
        }
    }
}