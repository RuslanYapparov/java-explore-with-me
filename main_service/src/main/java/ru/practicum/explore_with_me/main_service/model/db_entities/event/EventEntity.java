package ru.practicum.explore_with_me.main_service.model.db_entities.event;

import lombok.*;

import ru.practicum.explore_with_me.main_service.model.db_entities.CategoryEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.CompilationEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.UserEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "events")
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "compilations" })
@Getter
@Setter
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private long id;
    @Column(name = "event_title")
    private String title;
    @Column(name = "event_description")
    private String description;
    @Column(name = "event_annotation")
    private String annotation;
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    @Column(name = "event_state")
    private String state;
    @Embedded
    private GeoLocationEntity location;
    @Column(name = "participant_limit")
    private int participantLimit;
    @Column(name = "confirmed_requests")
    private int confirmedRequests;
    @Column(name = "event_rating")
    private int rating;
    @Column(name = "number_of_users_liked")
    private int numberOfUsersLiked;
    @Column(name = "paid")
    private boolean paid;
    @Column(name = "request_moderation")
    private boolean requestModeration;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id")
    private UserEntity initiator;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
    @JoinTable(
            name = "events_compilations",
            joinColumns = { @JoinColumn(name = "event_id") },
            inverseJoinColumns = { @JoinColumn(name = "compilation_id") }
    )
    private Set<CompilationEntity> compilations;

}