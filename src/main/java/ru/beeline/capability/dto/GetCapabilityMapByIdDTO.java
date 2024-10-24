package ru.beeline.capability.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class GetCapabilityMapByIdDTO {

    private String name;
    private String description;
    private Integer typeId;
    private List<GroupDTO> groups;
}
