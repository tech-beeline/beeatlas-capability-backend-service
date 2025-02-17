package ru.beeline.capability.cleint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.beeline.capability.exception.DocumentServerException;
import ru.beeline.capability.exception.NotFoundException;

import java.io.File;

@Slf4j
@Service
public class DocumentClient {

    RestTemplate restTemplate;
    private final String documentServerUrl;

    public DocumentClient(@Value("${integration.document-server-url}") String documentServerUrl,
                          RestTemplate restTemplate) {
        this.documentServerUrl = documentServerUrl;
        this.restTemplate = restTemplate;
    }

    public void patchExcelFile(Integer docId, File excelFile, String fileName) {
        try {
            String url = documentServerUrl + "/api/v1/export/" + docId;
            FileSystemResource resource = new FileSystemResource(excelFile);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, fileName);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity response = restTemplate.exchange(url, HttpMethod.PATCH, requestEntity, Object.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("File uploaded successfully");
            } else {
                log.error("Failed to upload file: {}", response.getStatusCode());
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException(e.getMessage());
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new DocumentServerException(e.getMessage());
        } catch (RestClientException e) {
            log.error("Error while uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Error while uploading file");
        }
    }
}