package ru.beeline.capability.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bc_group", schema = "capability")
public class BcGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bc_group_generator")
    @SequenceGenerator(name = "bc_group_generator", sequenceName = "bc_group_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "bc_id")
    private Integer bcId;

    @Column(name = "group_id")
    private Integer groupId;
}
