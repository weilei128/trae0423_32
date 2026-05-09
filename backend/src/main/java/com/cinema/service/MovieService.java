package com.cinema.service;

import com.cinema.entity.Movie;
import com.cinema.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class MovieService {

    private static final String MOVIE_CACHE_KEY = "cinema:movies";

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<Movie> getAllMovies() {
        return movieRepository.findByIsActiveTrue();
    }

    public List<Movie> searchMovies(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllMovies();
        }
        return movieRepository.findByTitleContainingIgnoreCaseAndIsActiveTrue(keyword);
    }

    public Optional<Movie> getMovieById(Long id) {
        String key = MOVIE_CACHE_KEY + ":" + id;
        Movie cached = (Movie) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Optional.of(cached);
        }
        Optional<Movie> movie = movieRepository.findById(id);
        movie.ifPresent(m -> redisTemplate.opsForValue().set(key, m, 1, TimeUnit.HOURS));
        return movie;
    }

    @Transactional
    public Movie saveMovie(Movie movie) {
        Movie saved = movieRepository.save(movie);
        redisTemplate.delete(MOVIE_CACHE_KEY + ":" + saved.getId());
        return saved;
    }

    @Transactional
    public void deleteMovie(Long id) {
        Optional<Movie> movie = movieRepository.findById(id);
        movie.ifPresent(m -> {
            m.setIsActive(false);
            movieRepository.save(m);
            redisTemplate.delete(MOVIE_CACHE_KEY + ":" + id);
        });
    }
}
