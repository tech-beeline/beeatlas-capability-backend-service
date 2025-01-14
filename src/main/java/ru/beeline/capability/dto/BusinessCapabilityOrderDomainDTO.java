package ru.beeline.capability.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityOrderDomainDTO {

    private String domainName;
    private Integer orderBcId;
}
