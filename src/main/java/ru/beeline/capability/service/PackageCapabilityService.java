package ru.beeline.capability.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.beeline.capability.dto.PackageRegistrationRequestDTO;
import ru.beeline.capability.dto.PostBusinessCapabilityDTO;
import ru.beeline.capability.dto.PostTechCapabilityDTO;
import ru.beeline.capability.dto.PackageRegistrationResponseDTO;
import ru.beeline.capability.exception.PackageRegistrationException;

import java.util.List;

import static ru.beeline.capability.utils.Constants.UPDATE_BUSINESS_CAPABILITIES_OPERATION;
import static ru.beeline.capability.utils.Constants.UPDATE_TECH_CAPABILITIES_OPERATION;
import static ru.beeline.capability.utils.RestHelper.getRestTemplate;

@Slf4j
@Service
public class PackageCapabilityService {

    private final String packLoaderServerUrl;
    private final String queueName;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public PackageCapabilityService(@Value("${integration.pack-loader-server-url}") String packLoaderServerUrl,
            @Value("${queue.package.name}") String queueName) {
        this.packLoaderServerUrl = packLoaderServerUrl;
        this.queueName = queueName;
    }

    public PackageRegistrationResponseDTO registerTechCapabilitiesPackage(List<PostTechCapabilityDTO> techCapabilities) {
        PackageRegistrationResponseDTO packageRegistrationResponseDTO = registerPackage(
                UPDATE_TECH_CAPABILITIES_OPERATION,
                techCapabilities.size());
        if(packageRegistrationResponseDTO != null) {
            try {
                sendMessageToQueue(packageRegistrationResponseDTO, techCapabilities);
                return packageRegistrationResponseDTO;
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        } else throw new PackageRegistrationException("Tech capability package was not be registered");
        return null;
    }

    public PackageRegistrationResponseDTO registerBusinessCapabilitiesPackage(List<PostBusinessCapabilityDTO> businessCapabilities) {
        PackageRegistrationResponseDTO packageRegistrationResponseDTO = registerPackage(
                UPDATE_BUSINESS_CAPABILITIES_OPERATION,
                businessCapabilities.size());
        if(packageRegistrationResponseDTO != null) {
            try {
                sendMessageToQueue(packageRegistrationResponseDTO, businessCapabilities);
                return packageRegistrationResponseDTO;
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        } else throw new PackageRegistrationException("Business capability package was not be registered");
        return null;
    }


    private void sendMessageToQueue(PackageRegistrationResponseDTO packageRegistrationResponseDTO, List<?> capabilities) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectNode messagePayload = objectMapper.createObjectNode();
        messagePayload.put("packageId", packageRegistrationResponseDTO.getPackageId());
        messagePayload.set("payload", objectMapper.valueToTree(capabilities));

        String message = objectMapper.writeValueAsString(messagePayload);
        sendMessageToTechCapabilityQueue(queueName, message);
    }

    private void sendMessageToTechCapabilityQueue(String queue, String message) {
        rabbitTemplate.convertAndSend(queue, message, messagePostProcessor -> {
            messagePostProcessor.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return messagePostProcessor;
        });
    }

    private PackageRegistrationResponseDTO registerPackage(String operation, int dataSize) {
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<PackageRegistrationRequestDTO> entity = new HttpEntity<>(PackageRegistrationRequestDTO.builder()
                    .operation(operation)
                    .count(dataSize)
                    .build(),
                    headers);
            final RestTemplate restTemplate = getRestTemplate();

            return restTemplate.exchange(packLoaderServerUrl + "/api/v1/package",
                    HttpMethod.POST, entity, PackageRegistrationResponseDTO.class).getBody();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

}
