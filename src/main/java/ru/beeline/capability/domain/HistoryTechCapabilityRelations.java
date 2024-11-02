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
    private Long id;

    @Column(name = "id_parent", nullable = false)
    private Long idParent;

    @Column(name = "id_history_child", nullable = false)
    private Long idHistoryChild;

}