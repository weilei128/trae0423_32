package com.cinema.controller;

import com.cinema.entity.TicketOrder;
import com.cinema.service.TicketOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class TicketOrderController {

    @Autowired
    private TicketOrderService ticketOrderService;

    @GetMapping
    public ResponseEntity<List<TicketOrder>> getAllOrders() {
        return ResponseEntity.ok(ticketOrderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        return ticketOrderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/orderNo/{orderNo}")
    public ResponseEntity<?> getOrderByOrderNo(@PathVariable String orderNo) {
        return ticketOrderService.getOrderByOrderNo(orderNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            TicketOrder order = new TicketOrder();
            order.setCustomerName(request.get("customerName").toString());
            order.setCustomerPhone(request.get("customerPhone").toString());

            Long scheduleId = Long.valueOf(request.get("scheduleId").toString());
            Long memberId = request.containsKey("memberId") && request.get("memberId") != null 
                    ? Long.valueOf(request.get("memberId").toString()) : null;
            
            @SuppressWarnings("unchecked")
            List<String> seats = (List<String>) request.get("seats");
            
            Integer pointsUsed = request.containsKey("pointsUsed") && request.get("pointsUsed") != null
                    ? Integer.valueOf(request.get("pointsUsed").toString()) : 0;

            TicketOrder created = ticketOrderService.createOrder(order, scheduleId, memberId, seats, pointsUsed);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long id) {
        try {
            TicketOrder order = ticketOrderService.payOrder(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOrder(@RequestBody Map<String, String> request) {
        try {
            String qrCode = request.get("qrCode");
            TicketOrder order = ticketOrderService.verifyOrder(qrCode);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<List<TicketOrder>> getOrdersByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(ticketOrderService.getOrdersByPhone(phone));
    }
}
