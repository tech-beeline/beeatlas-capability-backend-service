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
public class GroupDTO {

    private Integer groupId;
    private String nameGroup;
    private List<GetChildrenGroupsDTO> childrenGroup;
    private List<CapabilityDTO> capability;
}
