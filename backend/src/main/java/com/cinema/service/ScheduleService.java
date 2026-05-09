package com.cinema.service;

import com.cinema.entity.Hall;
import com.cinema.entity.Movie;
import com.cinema.entity.Schedule;
import com.cinema.repository.HallRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ScheduleRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private HallRepository hallRepository;

    private final Gson gson = new Gson();

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findByIsActiveTrueAndStartTimeBetweenOrderByStartTimeAsc(
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now().plusDays(30)
        );
    }

    public List<Schedule> getSchedulesByMovie(Long movieId) {
        return scheduleRepository.findByMovieIdAndIsActiveTrueAndStartTimeAfterOrderByStartTimeAsc(
                movieId, LocalDateTime.now()
        );
    }

    public List<Schedule> getSchedulesByHall(Long hallId) {
        return scheduleRepository.findByHallIdAndIsActiveTrueAndStartTimeAfterOrderByStartTimeAsc(
                hallId, LocalDateTime.now()
        );
    }

    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    @Transactional
    public Schedule createSchedule(Schedule schedule, Long movieId, Long hallId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("影片不存在"));
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new RuntimeException("影厅不存在"));

        schedule.setMovie(movie);
        schedule.setHall(hall);

        if (schedule.getEndTime() == null) {
            schedule.setEndTime(schedule.getStartTime().plusMinutes(movie.getDuration() + 30));
        }

        if (schedule.getPrice() == null) {
            schedule.setPrice(calculatePrice(hall, schedule.getStartTime()));
        }

        schedule.setTimeSlot(determineTimeSlot(schedule.getStartTime()));

        List<Schedule> conflicts = scheduleRepository.findConflictingSchedules(
                hall, schedule.getStartTime(), schedule.getEndTime()
        );
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("该时段已有排片");
        }

        schedule.setAvailableSeats(generateAvailableSeats(hall));
        schedule.setBookedSeats("[]");

        return scheduleRepository.save(schedule);
    }

    private Double calculatePrice(Hall hall, LocalDateTime startTime) {
        double basePrice = hall.getBasePrice();
        int hour = startTime.getHour();

        if (hour >= 9 && hour < 12) {
            basePrice *= 0.8;
        } else if (hour >= 12 && hour < 18) {
            basePrice *= 1.0;
        } else if (hour >= 18 && hour < 22) {
            basePrice *= 1.3;
        } else {
            basePrice *= 0.9;
        }

        if (hall.getType().equals("IMAX")) {
            basePrice *= 1.5;
        } else if (hall.getType().equals("杜比")) {
            basePrice *= 1.3;
        }

        return Math.round(basePrice * 100.0) / 100.0;
    }

    private String determineTimeSlot(LocalDateTime time) {
        int hour = time.getHour();
        if (hour >= 9 && hour < 12) return "早场";
        if (hour >= 12 && hour < 18) return "午场";
        if (hour >= 18 && hour < 22) return "晚场";
        return "夜场";
    }

    private String generateAvailableSeats(Hall hall) {
        return hall.getSeatMap();
    }

    @Transactional
    public Schedule updateSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        Optional<Schedule> schedule = scheduleRepository.findById(id);
        schedule.ifPresent(s -> {
            s.setIsActive(false);
            scheduleRepository.save(s);
        });
    }

    public List<String> getBookedSeats(Long scheduleId) {
        Optional<Schedule> schedule = scheduleRepository.findById(scheduleId);
        if (schedule.isPresent()) {
            String bookedSeats = schedule.get().getBookedSeats();
            if (bookedSeats != null && !bookedSeats.isEmpty()) {
                return gson.fromJson(bookedSeats, List.class);
            }
        }
        return Collections.emptyList();
    }

    @Transactional
    public boolean bookSeats(Long scheduleId, List<String> seats) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
        if (!scheduleOpt.isPresent()) {
            return false;
        }

        Schedule schedule = scheduleOpt.get();
        String availableSeatsJson = schedule.getAvailableSeats();
        JsonArray availableSeats = gson.fromJson(availableSeatsJson, JsonArray.class);

        for (String seatKey : seats) {
            String[] parts = seatKey.split("-");
            int row = Integer.parseInt(parts[0]);
            int seatNum = Integer.parseInt(parts[1]);

            boolean found = false;
            for (JsonElement rowElement : availableSeats) {
                JsonArray rowArray = rowElement.getAsJsonArray();
                for (JsonElement seatElement : rowArray) {
                    JsonObject seatObj = seatElement.getAsJsonObject();
                    if (seatObj.get("row").getAsInt() == row &&
                        seatObj.get("seat").getAsInt() == seatNum) {
                        if (!seatObj.get("available").getAsBoolean()) {
                            return false;
                        }
                        seatObj.addProperty("available", false);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            if (!found) return false;
        }

        schedule.setAvailableSeats(gson.toJson(availableSeats));

        String bookedSeatsJson = schedule.getBookedSeats();
        JsonArray bookedSeats = gson.fromJson(bookedSeatsJson != null ? bookedSeatsJson : "[]", JsonArray.class);
        for (String seat : seats) {
            bookedSeats.add(seat);
        }
        schedule.setBookedSeats(gson.toJson(bookedSeats));

        scheduleRepository.save(schedule);
        return true;
    }
}
