package com.jpastart.store.domain.member.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jpastart.store.domain.address.Address;
import com.jpastart.store.domain.order.entity.Order;
import lombok.*;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

//    @NotEmpty
    private String name;

    @Embedded
    private Address address;

//    @JsonManagedReference
    @JsonIgnore
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

//    @Override
//    public String toString() {
//        return "Member{" +
//                "id=" + id +
//                ", name='" + name + '\'' +
//                ", address=" + address +
//                ", orders=" + orders +
//                '}';
//    }
}
