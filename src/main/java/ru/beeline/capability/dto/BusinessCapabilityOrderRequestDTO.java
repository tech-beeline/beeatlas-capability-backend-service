package ru.beeline.capability.dto;

import com.sun.istack.NotNull;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCapabilityOrderRequestDTO {

    @NotNull
    private String name;
    @NotNull
    private String description;
    @NotNull
    private String owner;
    @NotNull
    private Integer parentId;
    @NotNull
    private String author;
    private Long mutableBcId;
    private String comment;

}
