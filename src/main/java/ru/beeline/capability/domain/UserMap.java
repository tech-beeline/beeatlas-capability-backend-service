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

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_map", schema = "capability")
public class UserMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "user_map_generator")
    @SequenceGenerator(name = "user_map_generator", sequenceName = "user_map_id_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    private Boolean author;

    @Column(name = "map_id")
    private Integer mapId;

    @ManyToOne
    @JoinColumn(name = "map_id", insertable = false, updatable = false)
    private Map map;
}
