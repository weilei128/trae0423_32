package com.cinema.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "movie")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "影片名称不能为空")
    @Size(max = 200, message = "影片名称不能超过200个字符")
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 1000, message = "简介不能超过1000个字符")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "时长不能为空")
    @Min(value = 1, message = "时长最小为1分钟")
    @Max(value = 300, message = "时长最大为300分钟")
    @Column(nullable = false)
    private Integer duration;

    @NotBlank(message = "类型不能为空")
    @Size(max = 50, message = "类型不能超过50个字符")
    @Column(nullable = false, length = 50)
    private String genre;

    @Size(max = 100, message = "导演不能超过100个字符")
    @Column(length = 100)
    private String director;

    @Size(max = 500, message = "主演不能超过500个字符")
    @Column(length = 500)
    private String actors;

    @NotNull(message = "上映日期不能为空")
    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @NotBlank(message = "分级不能为空")
    @Size(max = 20, message = "分级不能超过20个字符")
    @Column(nullable = false, length = 20)
    private String rating;

    @Column(length = 500)
    private String poster;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
