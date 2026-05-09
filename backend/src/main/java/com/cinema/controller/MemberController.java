package com.cinema.controller;

import com.cinema.entity.Member;
import com.cinema.entity.PointsRecord;
import com.cinema.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "*")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Member>> searchMembers(@RequestParam String keyword) {
        return ResponseEntity.ok(memberService.searchMembers(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMemberById(@PathVariable Long id) {
        return memberService.getMemberById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<?> getMemberByPhone(@PathVariable String phone) {
        return memberService.getMemberByPhone(phone)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody Member member) {
        try {
            return ResponseEntity.ok(memberService.registerMember(member));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMember(@PathVariable Long id, @RequestBody Member member) {
        return memberService.getMemberById(id)
                .map(existing -> {
                    member.setId(id);
                    if (member.getCreatedAt() == null) {
                        member.setCreatedAt(existing.getCreatedAt());
                    }
                    return ResponseEntity.ok(memberService.saveMember(member));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "会员已删除");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/points-history")
    public ResponseEntity<List<PointsRecord>> getPointsHistory(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getPointsHistory(id));
    }
}
