package ru.beeline.capability.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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

    private String status;
    private String author;
    private String link;

    @Column(name = "is_domain", nullable = false)
    private Boolean isDomain;

    @Column(nullable = false)
    private Long version;

}
