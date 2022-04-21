package com.jpastart.store.repository;

import com.jpastart.store.repository.member.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

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