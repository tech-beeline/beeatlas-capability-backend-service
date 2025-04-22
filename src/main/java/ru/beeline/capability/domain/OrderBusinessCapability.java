package ru.beeline.capability.domain;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_business_capability", schema = "capability")
public class OrderBusinessCapability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_business_capability_generator")
    @SequenceGenerator(name = "order_business_capability_generator", sequenceName = "order_business_capability_seq", allocationSize = 1)
    private Integer id;

    private String code;

    private String name;

    private String description;

    private String owner;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    private String status;

    @Column(name = "author")
    private String author;

    @Column(name = "is_domain")
    private Boolean isDomain;

    @Column(name = "business_key")
    private String businessKey;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "mutable_bc_id")
    private Integer mutableBcId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", insertable = false, updatable = false)
    private BusinessCapability parent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mutable_bc_id", referencedColumnName = "id", insertable = false, updatable = false)
    private BusinessCapability mutableBusinessCapability;
}
