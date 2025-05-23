package ru.beeline.capability.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityOrderDraftResponseDTO {

    private String name;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime updateDate;
    private String owner;
    private ParentOrMutableDTO parent;
    private String author;
    private ParentOrMutableDTO mutable;
}
