package ru.beeline.capability.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "enum_criterias", schema = "capability")
public class EnumCriteria {

    @Id
    private Long id;

    private String name;

    private String description;

    private String type;

    private Integer interval;

    private Integer threshold;
}
