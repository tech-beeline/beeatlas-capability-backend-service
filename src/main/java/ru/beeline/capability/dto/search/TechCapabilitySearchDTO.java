package ru.beeline.capability.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.beeline.capability.dto.product.GetProductsByIdsDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechCapabilitySearchDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private GetProductsByIdsDTO system;
    private List<CapabilityDomainDTO> parents;
}
