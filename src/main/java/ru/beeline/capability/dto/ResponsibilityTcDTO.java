package ru.beeline.capability.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponsibilityTcDTO {
    private List<ResponsibilityCapabilityDTO> responsibility;
    private List<ResponsibilityCapabilityDTO> implemented;

}
