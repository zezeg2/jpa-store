package com.jpastart.store.domain.order;

import com.jpastart.store.domain.delivery.Delivery;
import com.jpastart.store.domain.member.Member;
import com.jpastart.store.domain.orderitem.OrderItem;
import com.jpastart.store.domain.status.DeliveryStatus;
import com.jpastart.store.domain.status.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDateTime;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    /* 연관관계 편의 메서드, 양방향일때 한쪽에서 세팅을 해줌 */

    public void setMember(Member member){
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    /* 생성 메서드 */

    protected Order() {
    }

    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems){
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
//        order.setOrderItems(List.of(orderItems));
        order.setOrderStatus(OrderStatus.ORDER);
        Arrays.stream(orderItems).forEach(order::addOrderItem);
        order.setOrderDateTime(LocalDateTime.now());
        return order;

    }

    /* 비즈니스로직 */

    /* 주문 취소 로직 */
    public void cancel(){
        if (this.delivery.getDeliveryStatus() == DeliveryStatus.COMP) throw new IllegalStateException("이미 배송 완료된 상품입니다.");
        this.setOrderStatus(OrderStatus.CANCEL);

        for (OrderItem orderItem : this.orderItems) orderItem.cancel();
    }

    /* 조회 로직 */

    /* 전체 주문 가격 조회 로직 */
    public int getTotalPrice(){
        return orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }


}
