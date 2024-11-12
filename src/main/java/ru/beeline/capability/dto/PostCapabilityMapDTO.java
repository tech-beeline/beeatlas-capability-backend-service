package ru.beeline.capability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PostCapabilityMapDTO {

    private String name;

    private String description;

    private Integer typeId;
}
