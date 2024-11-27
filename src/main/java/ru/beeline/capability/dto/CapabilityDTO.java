package ru.beeline.capability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapabilityDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String author;
    private String status;
    private String link;
    private Date createdDate;
    private Date updatedDate;
    @JsonProperty("isDomain")
    private boolean isDomain;
    private String owner;
    @JsonProperty("responsibilityProductId")
    private Integer responsibilityProductId;
    private Integer parentId;
    private CriteriaDTO criteria;
}
