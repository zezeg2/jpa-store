package com.jpastart.store.domain.order.dto;

import com.jpastart.store.domain.address.Address;
import com.jpastart.store.domain.orderitem.dto.OrderItemQueryDto;
import com.jpastart.store.domain.status.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderFlatDto {

    private Long orderId;

    private String name;

    private LocalDateTime orderDate;

    private OrderStatus orderStatus;

    private Address address;

    private String itemName;

    private int orderPrice;

    private int count;
}
