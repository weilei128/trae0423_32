package com.cinema.controller;

import com.cinema.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyReport(
            @RequestParam(required = false) String date) {
        LocalDate reportDate = (date != null && !date.isEmpty()) 
                ? LocalDate.parse(date) 
                : LocalDate.now();
        return ResponseEntity.ok(reportService.getDailyReport(reportDate));
    }

    @GetMapping("/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyReport() {
        return ResponseEntity.ok(reportService.getWeeklyReport());
    }
}
