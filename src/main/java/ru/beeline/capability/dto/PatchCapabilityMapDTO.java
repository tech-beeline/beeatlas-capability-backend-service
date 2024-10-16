package ru.beeline.capability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PatchCapabilityMapDTO {

    private Long id;
    private String nameGroup;
    private List<Integer> capabilityIds;
    private List<ChildrenGroupDTO> childrenGroups;
}
