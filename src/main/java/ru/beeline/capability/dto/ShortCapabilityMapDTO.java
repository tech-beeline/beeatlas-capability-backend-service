package ru.beeline.capability.dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ShortCapabilityMapDTO {

    private Integer id;
    private String name;
    private String description;
    private Date createdDate;
    private Date updatedDate;
    private Integer typeId;
}
