package com.cinema.service;

import com.cinema.entity.Member;
import com.cinema.entity.PointsRecord;
import com.cinema.entity.Schedule;
import com.cinema.entity.TicketOrder;
import com.cinema.repository.MemberRepository;
import com.cinema.repository.PointsRecordRepository;
import com.cinema.repository.ScheduleRepository;
import com.cinema.repository.TicketOrderRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class TicketOrderService {

    private static final String QR_CACHE_PREFIX = "cinema:qr:";

    @Autowired
    private TicketOrderRepository ticketOrderRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PointsRecordRepository pointsRecordRepository;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final Gson gson = new Gson();

    public List<TicketOrder> getAllOrders() {
        return ticketOrderRepository.findAll();
    }

    public Optional<TicketOrder> getOrderById(Long id) {
        return ticketOrderRepository.findById(id);
    }

    public Optional<TicketOrder> getOrderByOrderNo(String orderNo) {
        return ticketOrderRepository.findByOrderNo(orderNo);
    }

    @Transactional
    public TicketOrder createOrder(TicketOrder order, Long scheduleId, Long memberId, List<String> seats, Integer pointsUsed) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("排片不存在"));

        Member member = null;
        if (memberId != null) {
            member = memberRepository.findById(memberId).orElse(null);
        }

        if (!scheduleService.bookSeats(scheduleId, seats)) {
            throw new RuntimeException("座位预订失败，可能已被占用");
        }

        order.setSchedule(schedule);
        order.setMember(member);
        order.setOrderNo(generateOrderNo());
        order.setSeats(gson.toJson(seats));
        order.setSeatCount(seats.size());
        order.setUnitPrice(schedule.getPrice());
        order.setTotalAmount(schedule.getPrice() * seats.size());
        order.setStatus("待支付");
        order.setIsVerified(false);

        if (pointsUsed != null && pointsUsed > 0 && member != null) {
            if (member.getPoints() < pointsUsed) {
                throw new RuntimeException("积分不足");
            }
            double discount = pointsUsed * 0.01;
            order.setPointsUsed(pointsUsed);
            order.setDiscountAmount(Math.min(discount, order.getTotalAmount()));
            order.setTotalAmount(Math.max(0, order.getTotalAmount() - order.getDiscountAmount()));
        }

        return ticketOrderRepository.save(order);
    }

    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String random = String.format("%04d", new Random().nextInt(10000));
        return "TK" + timestamp + random;
    }

    @Transactional
    public TicketOrder payOrder(Long orderId) {
        TicketOrder order = ticketOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!"待支付".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确");
        }

        order.setStatus("已支付");
        order.setPaidAt(LocalDateTime.now());
        order.setQrCode(generateQRCode(order));

        redisTemplate.opsForValue().set(QR_CACHE_PREFIX + order.getQrCode(), order.getId(), 24, TimeUnit.HOURS);

        Member member = order.getMember();
        if (member != null) {
            int pointsEarned = (int) (order.getTotalAmount() * 1);
            member.setPoints(member.getPoints() + pointsEarned - (order.getPointsUsed() != null ? order.getPointsUsed() : 0));
            memberRepository.save(member);

            PointsRecord record = new PointsRecord();
            record.setMember(member);
            record.setPoints(pointsEarned);
            record.setType("消费获得");
            record.setDescription("购票获得积分，订单号：" + order.getOrderNo());
            record.setOrderId(order.getId());
            pointsRecordRepository.save(record);

            if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
                PointsRecord useRecord = new PointsRecord();
                useRecord.setMember(member);
                useRecord.setPoints(-order.getPointsUsed());
                useRecord.setType("积分抵扣");
                useRecord.setDescription("使用积分抵扣，订单号：" + order.getOrderNo());
                useRecord.setOrderId(order.getId());
                pointsRecordRepository.save(useRecord);
            }
        }

        return ticketOrderRepository.save(order);
    }

    private String generateQRCode(TicketOrder order) {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    @Transactional
    public TicketOrder verifyOrder(String qrCode) {
        Object orderIdObj = redisTemplate.opsForValue().get(QR_CACHE_PREFIX + qrCode);
        if (orderIdObj == null) {
            throw new RuntimeException("二维码无效或已过期");
        }

        Long orderId = Long.valueOf(orderIdObj.toString());
        TicketOrder order = ticketOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (order.getIsVerified()) {
            throw new RuntimeException("订单已核销");
        }

        if (!"已支付".equals(order.getStatus())) {
            throw new RuntimeException("订单未支付");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(order.getSchedule().getEndTime())) {
            throw new RuntimeException("影片已结束");
        }

        order.setIsVerified(true);
        order.setVerifiedAt(LocalDateTime.now());
        redisTemplate.delete(QR_CACHE_PREFIX + qrCode);

        return ticketOrderRepository.save(order);
    }

    public List<TicketOrder> getOrdersByPhone(String phone) {
        return ticketOrderRepository.findByCustomerPhone(phone);
    }
}
