/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.domain;

import lombok.*;
import javax.persistence.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "promt", schema = "capability")
public class Promt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String alias;

    private String model;

    @Column(name = "promt")
    private String promt;
}
