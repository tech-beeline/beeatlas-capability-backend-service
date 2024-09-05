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
@Table(name = "sequence_tc", schema = "your_schema")
public class SequenceTc {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_tc_id_generator")
    @SequenceGenerator(name = "sequence_tc_id_generator", sequenceName = "sequence_seq_tc_id", allocationSize = 1)
    private Long id;

    @Column(name = "seq_id")
    private Long seqId;

    @Column(name = "tc_id")
    private Long tcId;
}