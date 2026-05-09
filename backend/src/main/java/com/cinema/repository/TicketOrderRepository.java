package com.cinema.repository;

import com.cinema.entity.TicketOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketOrderRepository extends JpaRepository<TicketOrder, Long> {
    Optional<TicketOrder> findByOrderNo(String orderNo);
    List<TicketOrder> findByScheduleId(Long scheduleId);
    List<TicketOrder> findByCustomerPhone(String phone);
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM TicketOrder o WHERE o.status = '已支付' AND o.createdAt BETWEEN :start AND :end")
    Double sumTotalAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COALESCE(SUM(o.seatCount), 0) FROM TicketOrder o WHERE o.status = '已支付' AND o.createdAt BETWEEN :start AND :end")
    Long sumSeatCountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT o FROM TicketOrder o WHERE o.status = '已支付' AND o.createdAt BETWEEN :start AND :end")
    List<TicketOrder> findPaidOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
