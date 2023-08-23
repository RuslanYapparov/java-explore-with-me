package ru.practicum.explore_with_me.main_service.model.db_entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;
    @Column(name = "user_name", nullable = false)
    private String name;
    @Column(name = "email", nullable = false)
    private String email;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "initiator")
    private List<EventEntity> events;

}