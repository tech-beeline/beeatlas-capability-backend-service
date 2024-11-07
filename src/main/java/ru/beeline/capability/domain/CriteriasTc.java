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
@Table(name = "criterias_tc", schema = "capability")
public class CriteriasTc {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "criterias_tc_id_generator")
    @SequenceGenerator(name = "criterias_tc_id_generator", sequenceName = "sequence_criterion_tc_id", allocationSize = 1)
    private Long id;

    @Column(name = "criterion_id")
    private Long criterionId;

    private Integer value;

    private Integer grade;

    @Column(name = "tc_id")
    private Long tcId;
}