package com.cinema.service;

import com.cinema.entity.Schedule;
import com.cinema.entity.TicketOrder;
import com.cinema.repository.HallRepository;
import com.cinema.repository.TicketOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private TicketOrderRepository ticketOrderRepository;

    @Autowired
    private HallRepository hallRepository;

    public Map<String, Object> getDailyReport(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("date", date.toString());

        Double totalRevenue = ticketOrderRepository.sumTotalAmountBetween(start, end);
        report.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);

        Long totalTickets = ticketOrderRepository.sumSeatCountBetween(start, end);
        report.put("totalTickets", totalTickets != null ? totalTickets : 0L);

        List<TicketOrder> orders = ticketOrderRepository.findPaidOrdersBetween(start, end);
        
        int totalSeatsAvailable = hallRepository.findByIsActiveTrue().stream()
                .mapToInt(h -> h.getTotalSeats()).sum();
        
        double occupancyRate = totalSeatsAvailable > 0 ? 
                (double) (totalTickets != null ? totalTickets : 0) / totalSeatsAvailable * 100 : 0;
        report.put("occupancyRate", Math.round(occupancyRate * 100.0) / 100.0);

        Map<Long, Double> movieRevenue = new HashMap<>();
        Map<Long, Integer> movieTickets = new HashMap<>();
        Map<Long, String> movieNames = new HashMap<>();

        for (TicketOrder order : orders) {
            Schedule schedule = order.getSchedule();
            if (schedule != null && schedule.getMovie() != null) {
                Long movieId = schedule.getMovie().getId();
                movieNames.put(movieId, schedule.getMovie().getTitle());
                movieRevenue.merge(movieId, order.getTotalAmount(), Double::sum);
                movieTickets.merge(movieId, order.getSeatCount(), Integer::sum);
            }
        }

        List<Map<String, Object>> movieStats = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : movieRevenue.entrySet()) {
            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("movieId", entry.getKey());
            stat.put("movieName", movieNames.get(entry.getKey()));
            stat.put("revenue", entry.getValue());
            stat.put("tickets", movieTickets.get(entry.getKey()));
            movieStats.add(stat);
        }

        movieStats.sort((a, b) -> Double.compare((Double) b.get("revenue"), (Double) a.get("revenue")));
        report.put("movieStats", movieStats);

        return report;
    }

    public Map<String, Object> getWeeklyReport() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        
        Map<String, Object> report = new LinkedHashMap<>();
        List<Map<String, Object>> dailyReports = new ArrayList<>();
        
        double totalWeeklyRevenue = 0;
        long totalWeeklyTickets = 0;

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> daily = getDailyReport(date);
            dailyReports.add(daily);
            totalWeeklyRevenue += (Double) daily.get("totalRevenue");
            totalWeeklyTickets += (Long) daily.get("totalTickets");
        }

        report.put("dailyReports", dailyReports);
        report.put("totalWeeklyRevenue", totalWeeklyRevenue);
        report.put("totalWeeklyTickets", totalWeeklyTickets);
        report.put("weekStart", weekStart.toString());
        report.put("weekEnd", today.toString());

        return report;
    }
}
