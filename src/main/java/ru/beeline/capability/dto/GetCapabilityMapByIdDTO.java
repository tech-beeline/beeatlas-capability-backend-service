package ru.beeline.capability.dto;

import lombok.*;
import ru.beeline.capability.domain.EntityType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class GetCapabilityMapByIdDTO {

    private String name;
    private String description;
    private EntityType type;
    private List<GroupDTO> groups;
}
