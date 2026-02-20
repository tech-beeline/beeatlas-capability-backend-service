/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.domain;

import lombok.*;

import javax.persistence.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "entity_type", schema = "capability")
public class EntityType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_type_id_generator")
    @SequenceGenerator(name = "entity_type_id_generator", sequenceName = "entity_type_id_seq", allocationSize = 1)
    private Long id;

    private String name;

    private String title;
}
