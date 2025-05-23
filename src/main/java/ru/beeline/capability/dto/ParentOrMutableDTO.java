package ru.beeline.capability.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentOrMutableDTO {
    private String code;
    private String name;
    private Long id;
}
