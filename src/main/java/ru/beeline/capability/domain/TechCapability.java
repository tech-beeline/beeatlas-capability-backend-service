package ru.beeline.capability.domain;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tech_capability")
public class TechCapability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "buisness_iteraction_id_generator")
    @SequenceGenerator(name = "buisness_iteraction_id_generator", sequenceName = "BI_id_seq", allocationSize = 1)
    private Long id;

    private String code;
    private String name;
    private String description;
    private Long owner;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(name = "deleted_date")
    private Date deletedDate;

    private String status;

    private String author;

    private String link;

}
