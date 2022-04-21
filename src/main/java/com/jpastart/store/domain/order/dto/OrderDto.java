package com.jpastart.store.domain.order.dto;

import com.jpastart.store.domain.address.Address;
import com.jpastart.store.domain.order.entity.Order;
import com.jpastart.store.domain.orderitem.dto.OrderItemDto;
import com.jpastart.store.domain.orderitem.entity.OrderItem;
import com.jpastart.store.domain.status.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    //    private List<OrderItem> orderItems;
    private List<OrderItemDto> orderItems;

    public OrderDto(Order order) {
        orderId = order.getId();
        name = order.getMember().getName();
        orderDate = order.getOrderDateTime();
        orderStatus = order.getOrderStatus();
        address = order.getDelivery().getAddress();
//        order.getOrderItems().forEach(o -> o.getItem().getName()); //  Order DTO 내부에서 OrderItem Entity가 노출됨
//        orderitems = order.getOrderItems();
        orderItems = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());

    }
}
