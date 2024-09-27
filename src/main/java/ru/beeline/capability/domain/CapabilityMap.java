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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "maps", schema = "capability")
public class CapabilityMap {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "maps_id_generator")
    @SequenceGenerator(name = "maps_id_generator", sequenceName = "maps_id_seq", allocationSize = 1)
    private Integer id;

    private String description;

    private String name;

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "update_date")
    private Date updateDate;

    @Column(name = "deleted_date")
    private Date deletedDate;

    @Column(name = "type_id")
    private Integer typeId;

    @ManyToOne
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private EntityType entityType;
}
