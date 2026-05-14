/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "enum_criterias_id_generator")
    @SequenceGenerator(name = "enum_criterias_id_generator", sequenceName = "sequence_enum_criterias_id", allocationSize = 1)
    private Long id;

    private String name;

    private String description;

    private String type;

    private Integer interval;

    private Integer threshold;

    private Boolean revers;

    @Column(name = "min_desc")
    private String minDesc;

    @Column(name = "max_desc")
    private String maxDesc;

}
