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
@Table(name = "tech_capability_relations", schema = "capability")
public class TechCapabilityRelations {

    @Id
    @Column(name = "id_rel")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tech_capability_relations_id_generator")
    @SequenceGenerator(name = "tech_capability_relations_id_generator", sequenceName = "TCR_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_parent")
    private BusinessCapability businessCapability;

    @ManyToOne
    @JoinColumn(name = "id_child")
    private TechCapability techCapability;
}
