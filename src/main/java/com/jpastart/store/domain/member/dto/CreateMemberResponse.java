package com.jpastart.store.domain.member.dto;

import lombok.Data;

@Data
public class CreateMemberResponse {
    private Long id;

    private String name;

    public CreateMemberResponse(Long id) {

    }
}
