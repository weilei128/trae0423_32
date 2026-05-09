package com.cinema.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "hall")
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "影厅名称不能为空")
    @Size(max = 100, message = "影厅名称不能超过100个字符")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "设备类型不能为空")
    @Size(max = 50, message = "设备类型不能超过50个字符")
    @Column(nullable = false, length = 50)
    private String type;

    @NotNull(message = "座位总数不能为空")
    @Min(value = 1, message = "座位总数最小为1")
    @Max(value = 500, message = "座位总数最大为500")
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @NotNull(message = "行数不能为空")
    @Min(value = 1, message = "行数最小为1")
    @Max(value = 50, message = "行数最大为50")
    @Column(name = "rows_count", nullable = false)
    private Integer rowsCount;

    @NotNull(message = "每行座位数不能为空")
    @Min(value = 1, message = "每行座位数最小为1")
    @Max(value = 30, message = "每行座位数最大为30")
    @Column(name = "seats_per_row", nullable = false)
    private Integer seatsPerRow;

    @Size(max = 5000, message = "座位图不能超过5000个字符")
    @Column(columnDefinition = "TEXT")
    private String seatMap;

    @Size(max = 1000, message = "描述不能超过1000个字符")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "基础票价不能为空")
    @DecimalMin(value = "0.01", message = "基础票价最小为0.01")
    @DecimalMax(value = "1000", message = "基础票价最大为1000")
    @Column(name = "base_price", nullable = false)
    private Double basePrice = 30.0;

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
