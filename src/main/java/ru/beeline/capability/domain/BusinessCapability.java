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
@Table(name = "business_capability", schema = "capability")
public class BusinessCapability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "business_capability_id_generator")
    @SequenceGenerator(name = "business_capability_id_generator", sequenceName = "BC_id_seq", allocationSize = 1)
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
    @Column(name = "parent_id")
    private Long parentId;
    private String source;

    @ManyToOne
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private BusinessCapability parentEntity;

    private String link;
    @Column(name = "is_domain")
    private boolean isDomain;

    @OneToMany
    @JoinColumn(name = "id_parent")
    private List<TechCapabilityRelations> children;

    @OneToMany(mappedBy = "bcId")
    private List<BusinessCapabilityCriteria> criteria;

    @Transient
    private List<BusinessCapability> childrenOfTree;

    @Override
    public String toString() {
        return "BusinessCapability{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", owner='" + owner + '\'' +
                ", status='" + status + '\'' +
                ", author='" + author + '\'' +
                ", parentId=" + parentId +
                ", link='" + link + '\'' +
                ", isDomain=" + isDomain +
                '}';
    }
}
