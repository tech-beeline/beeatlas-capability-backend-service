package ru.beeline.capability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetChildrenGroupsDTO {

    private Integer groupId;
    private String nameGroup;
    private Integer parentId;
    private List<CapabilityDTO> capability;
}
