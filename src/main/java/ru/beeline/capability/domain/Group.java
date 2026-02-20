/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "groups", schema = "capability")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "groups_generator")
    @SequenceGenerator(name = "groups_generator", sequenceName = "groups_id_seq", allocationSize = 1)
    private Integer id;

    private String name;

    @Column(name = "map_id")
    private Integer mapId;

    @Column(name = "parent_id")
    private String parentId;
}
