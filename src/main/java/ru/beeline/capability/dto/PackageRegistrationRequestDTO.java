package ru.beeline.capability.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageRegistrationRequestDTO {
    private String operation;
    private int count;
}
