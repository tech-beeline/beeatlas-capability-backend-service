package ru.beeline.capability.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class NameAndDescriptionDTO {

    private String name;
    private String description;
}
