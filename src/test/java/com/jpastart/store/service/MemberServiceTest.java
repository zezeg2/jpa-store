package com.jpastart.store.service;

import com.jpastart.store.domain.member.entity.Member;
import com.jpastart.store.repository.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private EntityManager em;

    @DisplayName("1.회원가입")
    @Test
//    @Rollback(value = false)
    void test_1() {
        // given
        Member member = new Member();
        member.setName("henry");

        // when
        Long savedId = memberService.join(member);

        // than
        em.flush();
        assertEquals(member, memberRepository.findOne(savedId));
    }

    @DisplayName("2.중복 회원예외")
    @Test
    void test_2() {
        // given
        Member member1 = new Member();
        member1.setName("henry");

        Member member2 = new Member();
        member2.setName("henry");

        // when
        memberService.join(member1);

        // than
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> memberService.join(member2));
        assertEquals("이미 존재하는 회원입니다.", thrown.getMessage());


    }

}