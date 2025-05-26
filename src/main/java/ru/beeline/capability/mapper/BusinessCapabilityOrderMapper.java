package ru.beeline.capability.mapper;

import org.springframework.stereotype.Component;
import ru.beeline.capability.domain.OrderBusinessCapability;
import ru.beeline.capability.dto.BusinessCapabilityOrderDraftResponseDTO;
import ru.beeline.capability.dto.ParentOrMutableDTO;

@Component
public class BusinessCapabilityOrderMapper {

    public static BusinessCapabilityOrderDraftResponseDTO getBusinessCapabilityOrderDraftResponseDTO(
            OrderBusinessCapability order) {

        ParentOrMutableDTO parentDto = null;
        if (order.getParent() != null) {
            parentDto = ParentOrMutableDTO.builder()
                    .id(order.getParent().getId())
                    .code(order.getParent().getCode())
                    .name(order.getParent().getName())
                    .build();
        }

        ParentOrMutableDTO mutableDto = null;
        if (order.getMutableBusinessCapability() != null) {
            mutableDto = ParentOrMutableDTO.builder()
                    .id(order.getMutableBusinessCapability().getId())
                    .code(order.getMutableBusinessCapability().getCode())
                    .name(order.getMutableBusinessCapability().getName())
                    .build();
        }

        return BusinessCapabilityOrderDraftResponseDTO.builder()
                .name(order.getName())
                .description(order.getDescription())
                .createdDate(order.getCreatedDate())
                .updateDate(order.getLastModifiedDate())
                .owner(order.getOwner())
                .parent(parentDto)
                .author(order.getAuthor())
                .mutable(mutableDto)
                .build();
    }
}