package com.cinema.controller;

import com.cinema.entity.Schedule;
import com.cinema.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Schedule>> getSchedulesByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByMovie(movieId));
    }

    @GetMapping("/hall/{hallId}")
    public ResponseEntity<List<Schedule>> getSchedulesByHall(@PathVariable Long hallId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByHall(hallId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getScheduleById(@PathVariable Long id) {
        return scheduleService.getScheduleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/booked-seats")
    public ResponseEntity<List<String>> getBookedSeats(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getBookedSeats(id));
    }

    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody Map<String, Object> request) {
        try {
            Schedule schedule = new Schedule();
            Long movieId = Long.valueOf(request.get("movieId").toString());
            Long hallId = Long.valueOf(request.get("hallId").toString());
            java.time.LocalDateTime startTime = java.time.LocalDateTime.parse(request.get("startTime").toString());
            
            schedule.setStartTime(startTime);
            
            if (request.containsKey("endTime")) {
                schedule.setEndTime(java.time.LocalDateTime.parse(request.get("endTime").toString()));
            }
            if (request.containsKey("price")) {
                schedule.setPrice(Double.valueOf(request.get("price").toString()));
            }

            Schedule created = scheduleService.createSchedule(schedule, movieId, hallId);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        return scheduleService.getScheduleById(id)
                .map(existing -> {
                    schedule.setId(id);
                    return ResponseEntity.ok(scheduleService.updateSchedule(schedule));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "排片已删除");
        return ResponseEntity.ok(response);
    }
}
