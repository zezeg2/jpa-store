package com.jpastart.store.domain.order.dto;

import com.jpastart.store.domain.address.Address;
import com.jpastart.store.domain.order.entity.Order;
import com.jpastart.store.domain.status.OrderStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class SimpleOrderDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public SimpleOrderDto(Order order) {
        orderId = order.getId();
        name = order.getMember().getName(); // LAZY 초기화
        orderDate = order.getOrderDateTime();
        orderStatus = order.getOrderStatus();
        address = order.getDelivery().getAddress(); // LAZY 초기화

    }
}
