package com.cinema.repository;

import com.cinema.entity.PointsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsRecordRepository extends JpaRepository<PointsRecord, Long> {
    List<PointsRecord> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
