package ru.beeline.capability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    private Integer id;
    private String name;
    private String alias;
    private String description;
    private String gitUrl;
    private String structurizrWorkspaceName;
    private String structurizrApiKey;
    private String structurizrApiSecret;
    private String structurizrApiUrl;
}
