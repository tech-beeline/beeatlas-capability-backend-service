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
@Table(name = "history_tech_capability", schema = "capability")
public class HistoryTechCapability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "business_capability_id_generator")
    @SequenceGenerator(name = "business_capability_id_generator", sequenceName = "history_bc_id_seq", allocationSize = 1)
    private Long id;
    @Column(name = "id_ref", nullable = false)
    private Long idRef;
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
    @Column(nullable = false)
    private Integer version;
    private String source;
    @Column(name = "responsibility_product_id")
    private Integer responsibilityProductId;

}