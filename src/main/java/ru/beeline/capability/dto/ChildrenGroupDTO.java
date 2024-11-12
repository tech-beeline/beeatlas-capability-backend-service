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
public class ChildrenGroupDTO {

    private Long childrenGroupId;
    private String nameGroup;
    private List<Integer> capabilityId;
}
