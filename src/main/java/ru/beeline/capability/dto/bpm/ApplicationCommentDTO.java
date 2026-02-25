/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto.bpm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationCommentDTO {

    private Integer id;
    private String comment;
    @JsonProperty("created_date")
    private LocalDateTime createdDate;
    @JsonProperty("full_name")
    private String fullName;
}
