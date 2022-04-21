package com.jpastart.store.domain.order.dto;

import com.jpastart.store.domain.address.Address;
import com.jpastart.store.domain.order.entity.Order;
import com.jpastart.store.domain.status.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SimpleOrderQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

}
