package ru.practicum.explore_with_me.stats_service.server_submodule.dao;

import lombok.*;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "hits")
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@Setter
public class HitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hit_id")
    private BigInteger id;
    @Column(name = "application")
    private String application;
    @Column(name = "uri")
    private String uri;
    @Column(name = "ip")
    private String ip;
    @Column(name = "hit_timestamp")
    private LocalDateTime timestamp;

}