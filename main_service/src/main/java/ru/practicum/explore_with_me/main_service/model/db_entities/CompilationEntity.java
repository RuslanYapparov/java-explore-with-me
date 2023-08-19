package ru.practicum.explore_with_me.main_service.model.db_entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "compilations")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = { "events" })
public class CompilationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compilation_id")
    private long id;
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "compilations")
    private Set<EventEntity> events;
    @Column(name = "compilation_title")
    private String title;
    @Column(name = "pinned")
    private boolean pinned;

}