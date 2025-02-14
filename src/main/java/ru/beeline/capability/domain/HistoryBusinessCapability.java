package ru.beeline.capability.domain;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "history_busines_capability", schema = "capability")
public class HistoryBusinessCapability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_bc_id_generator")
    @SequenceGenerator(name = "history_bc_id_generator", sequenceName = "history_bc_id_seq", allocationSize = 1)
    private Long id;
    @Column(name = "id_ref", nullable = false)
    private Long idRef;
    @Column(name = "parent_id")
    private Long parentId;
    private String code;
    @Column(nullable = false)
    private String name;
    private String description;
    private String owner;
    @Column(name = "modified_date", nullable = false)
    private Date modifiedDate;
    @Column(name = "deleted_date")
    private Date deletedDate;
    private String status;
    private String author;
    private String link;
    @Column(name = "is_domain", nullable = false)
    private Boolean isDomain;
    @Column(nullable = false)
    private Long version;
    private String source;
}
