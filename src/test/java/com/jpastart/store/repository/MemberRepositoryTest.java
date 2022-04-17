package com.jpastart.store.repository;

import com.jpastart.store.domain.member.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;

//    @DisplayName("1.")
//    @Test
//    @Transactional
//    @Rollback(value = false)
//    void test_1() {
//        // given
//        // when
//        // than
//        Member member = new Member();
//        member.setName("A");
//        Long saveId = memberRepository.save(member);
//
//        Member findMember = memberRepository.find(saveId);
//
//        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
//        Assertions.assertThat(findMember.getName()).isEqualTo(member.getName());
//        Assertions.assertThat(findMember).isEqualTo(member);
//
//    }

}