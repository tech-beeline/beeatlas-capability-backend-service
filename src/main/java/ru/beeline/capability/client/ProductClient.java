package ru.beeline.capability.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.beeline.capability.controller.RequestContext;
import ru.beeline.capability.dto.ProductDTO;
import ru.beeline.fdmlib.dto.product.GetProductsByIdsDTO;

import java.util.Arrays;
import java.util.List;

import static ru.beeline.capability.utils.Constants.*;

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
            ResponseEntity<ProductDTO> response = restTemplate.exchange(productServerUrl + "/api/v1/product/" + targetSystemCode,
                                                                        HttpMethod.GET,
                                                                        new HttpEntity<>(headers),
                                                                        new ParameterizedTypeReference<ProductDTO>() {});
            log.info("response from Product ServerUrl: " + response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public List<GetProductsByIdsDTO> getProductsByIds(List<Integer> ids) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(USER_ID_HEADER, RequestContext.getUserId());
            headers.set(USER_PERMISSION_HEADER, RequestContext.getUserPermissions().toString());
            headers.set(USER_PRODUCTS_IDS_HEADER, RequestContext.getUserProducts().toString());
            headers.set(USER_ROLES_HEADER, RequestContext.getRoles().toString());
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<List<GetProductsByIdsDTO>> response = restTemplate.exchange(productServerUrl + "/api/v1/product/by-ids?ids=" + Arrays.toString(
                                                                                               ids.toArray()),
                                                                                       HttpMethod.GET,
                                                                                       new HttpEntity<>(headers),
                                                                                       new ParameterizedTypeReference<List<GetProductsByIdsDTO>>() {});
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
