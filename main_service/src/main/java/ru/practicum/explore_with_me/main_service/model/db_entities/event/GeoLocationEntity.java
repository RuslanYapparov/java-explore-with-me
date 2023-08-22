package ru.practicum.explore_with_me.main_service.model.db_entities.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;

@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Embeddable
@Table(name = "events")
public class GeoLocationEntity {
    @Column(name = "geo_latitude")
    private double latitude;
    @Column(name = "geo_longitude")
    private double longitude;
}