package com.cinema.controller;

import com.cinema.entity.Hall;
import com.cinema.service.HallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/halls")
@CrossOrigin(origins = "*")
public class HallController {

    @Autowired
    private HallService hallService;

    @GetMapping
    public ResponseEntity<List<Hall>> getAllHalls() {
        return ResponseEntity.ok(hallService.getAllHalls());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Hall>> getHallsByType(@PathVariable String type) {
        return ResponseEntity.ok(hallService.getHallsByType(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHallById(@PathVariable Long id) {
        return hallService.getHallById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Hall> createHall(@Valid @RequestBody Hall hall) {
        return ResponseEntity.ok(hallService.saveHall(hall));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHall(@PathVariable Long id, @Valid @RequestBody Hall hall) {
        return hallService.getHallById(id)
                .map(existing -> {
                    hall.setId(id);
                    if (hall.getCreatedAt() == null) {
                        hall.setCreatedAt(existing.getCreatedAt());
                    }
                    return ResponseEntity.ok(hallService.saveHall(hall));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteHall(@PathVariable Long id) {
        hallService.deleteHall(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "影厅已删除");
        return ResponseEntity.ok(response);
    }
}
