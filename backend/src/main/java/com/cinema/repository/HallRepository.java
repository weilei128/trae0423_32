package com.cinema.repository;

import com.cinema.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {
    List<Hall> findByIsActiveTrue();
    List<Hall> findByTypeAndIsActiveTrue(String type);
}
