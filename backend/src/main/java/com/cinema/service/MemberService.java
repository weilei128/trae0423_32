package com.cinema.service;

import com.cinema.entity.Member;
import com.cinema.entity.PointsRecord;
import com.cinema.repository.MemberRepository;
import com.cinema.repository.PointsRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PointsRecordRepository pointsRecordRepository;

    public List<Member> getAllMembers() {
        return memberRepository.findByIsActiveTrue();
    }

    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    public Optional<Member> getMemberByPhone(String phone) {
        return memberRepository.findByPhone(phone);
    }

    public List<Member> searchMembers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllMembers();
        }
        return memberRepository.findByNameContainingIgnoreCaseOrPhoneContaining(keyword, keyword);
    }

    @Transactional
    public Member saveMember(Member member) {
        updateMemberLevel(member);
        return memberRepository.save(member);
    }

    @Transactional
    public Member registerMember(Member member) {
        Optional<Member> existing = memberRepository.findByPhone(member.getPhone());
        if (existing.isPresent()) {
            throw new RuntimeException("该手机号已注册");
        }
        member.setPoints(0);
        member.setLevel("普通会员");
        member.setIsActive(true);
        return memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(Long id) {
        Optional<Member> member = memberRepository.findById(id);
        member.ifPresent(m -> {
            m.setIsActive(false);
            memberRepository.save(m);
        });
    }

    public List<PointsRecord> getPointsHistory(Long memberId) {
        return pointsRecordRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    private void updateMemberLevel(Member member) {
        int points = member.getPoints();
        if (points >= 10000) {
            member.setLevel("钻石会员");
        } else if (points >= 5000) {
            member.setLevel("黄金会员");
        } else if (points >= 2000) {
            member.setLevel("白银会员");
        } else {
            member.setLevel("普通会员");
        }
    }
}
