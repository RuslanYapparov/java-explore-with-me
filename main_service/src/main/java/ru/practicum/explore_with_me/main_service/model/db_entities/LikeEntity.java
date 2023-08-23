package ru.practicum.explore_with_me.main_service.model.db_entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_likes")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class LikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private BigInteger id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private EventEntity event;
    @Column(name = "is_like")
    private boolean isLike;
    @Column(name = "clicked_on")
    private LocalDateTime clickedOn;

}