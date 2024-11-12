package ru.beeline.capability.domain;

import lombok.*;

import javax.persistence.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "history_tech_capability_relations", schema = "capability")
public class HistoryTechCapabilityRelations {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_bc_id_generator")
    @SequenceGenerator(name = "history_bc_id_generator", sequenceName = "history_bc_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "id_parent", nullable = false)
    private Long idParent;

    @Column(name = "id_history_child", nullable = false)
    private Long idHistoryChild;

}