package com.jpastart.store.domain.orderitem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jpastart.store.domain.item.Item;
import com.jpastart.store.domain.order.entity.Order;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;

    private int count;

    /* 생성메서드 */

    public static OrderItem createOrderItem(Item item, int orderPrice, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);
        return orderItem;
    }

    /* 비즈니스로직 */
    public void cancel() {
        this.getItem().addStock(count);
    }

    /* 조회 로직*/
    public int getTotalPrice() {
        return this.getOrderPrice() * this.getCount();
    }
}
