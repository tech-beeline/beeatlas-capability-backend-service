package ru.beeline.capability.cleint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.beeline.capability.dto.PackageRegistrationRequestDTO;
import ru.beeline.capability.dto.PackageRegistrationResponseDTO;

import static ru.beeline.capability.utils.RestHelper.getRestTemplate;

@Slf4j
@Service
public class PackageClient {
    private final String packLoaderServerUrl;
    public PackageClient(@Value("${integration.pack-loader-server-url}") String packLoaderServerUrl) {
        this.packLoaderServerUrl = packLoaderServerUrl;
    }
    public PackageRegistrationResponseDTO registerPackage(String operation, int dataSize) {
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
