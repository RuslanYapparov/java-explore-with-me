package ru.practicum.explore_with_me.stats_service.server_submodule.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<HitEntity, BigInteger> {

    @Query("select new ru.practicum.explore_with_me.stats_service.server_submodule.dao.UriStatFromDb(" +
            "h.application, h.uri, count(h.id)) " +
            "from HitEntity as h " +
            "where (h.timestamp between ?1 and ?2) " +
            "group by (h.uri,h.application) " +
            "order by count(h.id) desc")
    List<UriStatFromDb> getAllUriStatsOrderedByHitsCount(
            LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.explore_with_me.stats_service.server_submodule.dao.UriStatFromDb(" +
            "h.application, h.uri, count(h.id)) " +
            "from HitEntity as h " +
            "where h.uri in ?3 and " +
            "(h.timestamp between ?1 and ?2) " +
            "group by (h.uri,h.application) " +
            "order by count(h.id) desc")
    List<UriStatFromDb> getAllUriStatsOrderedByHitsCountWithUrisArray(
            LocalDateTime start, LocalDateTime end, String[] uris);

    @Query("select new ru.practicum.explore_with_me.stats_service.server_submodule.dao.UriStatFromDb(" +
            "h.application, h.uri, count(distinct h.ip)) " +
            "from HitEntity as h " +
            "where (h.timestamp between ?1 and ?2) " +
            "group by (h.uri, h.application) " +
            "order by count(distinct h.ip) desc")
    List<UriStatFromDb> getUniqueUriStatsOrderedByHitsCount(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.explore_with_me.stats_service.server_submodule.dao.UriStatFromDb(" +
            "h.application, h.uri, count(distinct h.ip)) " +
            "from HitEntity as h " +
            "where h.uri in ?3 and " +
            "(h.timestamp between ?1 and ?2) " +
            "group by (h.uri, h.application) " +
            "order by count(distinct h.ip) desc")
    List<UriStatFromDb> getUniqueUriStatsOrderedByHitsCountWithUrisArray(
            LocalDateTime start, LocalDateTime end, String[] uris);

}