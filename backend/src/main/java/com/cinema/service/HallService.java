package com.cinema.service;

import com.cinema.entity.Hall;
import com.cinema.repository.HallRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HallService {

    @Autowired
    private HallRepository hallRepository;

    private final Gson gson = new Gson();

    public List<Hall> getAllHalls() {
        return hallRepository.findByIsActiveTrue();
    }

    public List<Hall> getHallsByType(String type) {
        return hallRepository.findByTypeAndIsActiveTrue(type);
    }

    public Optional<Hall> getHallById(Long id) {
        return hallRepository.findById(id);
    }

    @Transactional
    public Hall saveHall(Hall hall) {
        if (hall.getSeatMap() == null || hall.getSeatMap().isEmpty()) {
            hall.setSeatMap(generateDefaultSeatMap(hall.getRowsCount(), hall.getSeatsPerRow()));
        }
        return hallRepository.save(hall);
    }

    private String generateDefaultSeatMap(int rows, int seatsPerRow) {
        JsonArray map = new JsonArray();
        for (int r = 0; r < rows; r++) {
            JsonArray row = new JsonArray();
            for (int s = 0; s < seatsPerRow; s++) {
                JsonObject seat = new JsonObject();
                seat.addProperty("row", r + 1);
                seat.addProperty("seat", s + 1);
                seat.addProperty("available", true);
                row.add(seat);
            }
            map.add(row);
        }
        return gson.toJson(map);
    }

    @Transactional
    public void deleteHall(Long id) {
        Optional<Hall> hall = hallRepository.findById(id);
        hall.ifPresent(h -> {
            h.setIsActive(false);
            hallRepository.save(h);
        });
    }
}
