package ru.beeline.capability.domain;

import lombok.*;

import javax.persistence.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "find_name_sort_table", schema = "capability")
public class FindNameSortTable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "find_name_sort_table_id_generator")
    @SequenceGenerator(name = "find_name_sort_table_id_generator", sequenceName = "find_name_sort_table_id_seq", allocationSize = 1)
    private Long id;

    private String vector;

    @OneToOne
    @JoinColumn(name = "type_id")
    private EntityType type;

    @Column(name = "ref_id")
    private Long refId;
}
