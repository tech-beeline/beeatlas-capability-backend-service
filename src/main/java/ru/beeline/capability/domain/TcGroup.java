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
@Table(name = "tc_group", schema = "capability")
public class TcGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tc_group_generator")
    @SequenceGenerator(name = "tc_group_generator", sequenceName = "tc_group_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "tc_id")
    private Integer tcId;

    @Column(name = "group_id")
    private Integer groupId;
}
