package ru.beeline.capability.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.beeline.capability.dto.ProductDTO;

@Slf4j
@Service
public class ProductClient {

    RestTemplate restTemplate;
    private final String productServerUrl;

    public ProductClient(@Value("${integration.product-server-url}") String productServerUrl,
                         RestTemplate restTemplate) {
        this.productServerUrl = productServerUrl;
        this.restTemplate = restTemplate;
    }

    public ProductDTO getProduct(String targetSystemCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            log.info("request to Product ServerUrl with targetSystemCode: " + targetSystemCode);
            ResponseEntity<ProductDTO> response = restTemplate.exchange(
                    productServerUrl + "/api/v1/product/" + targetSystemCode,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<ProductDTO>() {
                    });
            log.info("response from Product ServerUrl: " + response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
