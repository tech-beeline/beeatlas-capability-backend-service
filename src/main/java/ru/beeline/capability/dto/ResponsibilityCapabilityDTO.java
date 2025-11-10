package ru.beeline.capability.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponsibilityCapabilityDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
}
