package ru.beeline.capability.domain;

import lombok.*;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tech_capability", schema = "capability")
public class TechCapability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tech_capability_id_generator")
    @SequenceGenerator(name = "tech_capability_id_generator", sequenceName = "TC_id_seq", allocationSize = 1)
    private Long id;

    private String code;
    private String name;
    private String description;
    private String owner;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(name = "deleted_date")
    private Date deletedDate;

    private String status;

    private String author;

    private String link;

    @OneToMany
    @JoinColumn(name = "id_child")
    private List<TechCapabilityRelations> parents;
}
