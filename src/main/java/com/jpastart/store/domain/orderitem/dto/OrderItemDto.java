package com.jpastart.store.domain.orderitem.dto;

import com.jpastart.store.domain.orderitem.entity.OrderItem;
import lombok.Data;

@Data
public class OrderItemDto {

    private String itemName;

    private int orderPrice;

    private int count;

    public OrderItemDto(OrderItem orderItem) {

        itemName = orderItem.getItem().getName();

        orderPrice = orderItem.getOrderPrice();

        count = orderItem.getCount();
    }
}
