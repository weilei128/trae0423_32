package com.cinema.repository;

import com.cinema.entity.Hall;
import com.cinema.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByHallAndIsActiveTrue(Hall hall);
    
    @Query("SELECT s FROM Schedule s WHERE s.hall = :hall AND s.isActive = true " +
           "AND ((s.startTime BETWEEN :startTime AND :endTime) " +
           "OR (s.endTime BETWEEN :startTime AND :endTime) " +
           "OR (s.startTime <= :startTime AND s.endTime >= :endTime))")
    List<Schedule> findConflictingSchedules(
            @Param("hall") Hall hall,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<Schedule> findByMovieIdAndIsActiveTrueAndStartTimeAfterOrderByStartTimeAsc(Long movieId, LocalDateTime time);
    
    List<Schedule> findByHallIdAndIsActiveTrueAndStartTimeAfterOrderByStartTimeAsc(Long hallId, LocalDateTime time);
    
    List<Schedule> findByIsActiveTrueAndStartTimeBetweenOrderByStartTimeAsc(LocalDateTime start, LocalDateTime end);
}
