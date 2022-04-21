package com.jpastart.store.api;

import com.jpastart.store.domain.member.dto.*;
import com.jpastart.store.domain.member.entity.Member;
import com.jpastart.store.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;


    /* Entity를 직접 노출할 경우 json 직렬화 순환참조 문제로 인한 스택 오버플로우 발생 */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        return  memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result memberV2(){
        List<Member> findMembers = memberService.findMembers();
        List<findMemberDto> collect = findMembers.stream()
                .map(m -> new findMemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result(collect.size(),collect);

    }

    /**
     * Entity를 그대로 사용할경우 엔티티의 속성값이 변하게 되면 api스펙이 변경될 수 있음
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * Dto를 사용할 경우 엔티티의 속성값이 변경되더라도 api 스펙자체는 변경되지 않는다.
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request){
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return  new UpdateMemberResponse(findMember.getId(), findMember.getName());

    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

}
