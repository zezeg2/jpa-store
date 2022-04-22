package com.jpastart.store.service;

import com.jpastart.store.domain.delivery.Delivery;
import com.jpastart.store.domain.item.Item;
import com.jpastart.store.domain.member.entity.Member;
import com.jpastart.store.domain.order.entity.Order;
import com.jpastart.store.domain.orderitem.entity.OrderItem;
import com.jpastart.store.repository.item.ItemRepository;
import com.jpastart.store.repository.member.MemberRepository;
import com.jpastart.store.repository.order.OrderRepository;
import com.jpastart.store.repository.order.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    // 주문
    @Transactional
    public Long order(Long memberId, Long itemId, int count){
        // 엔티티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        // 배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        // 주문 저 -> cascade 옵션에 의해 orderItem, delivery 도 함께 persist 되다.
        orderRepository.save(order);

        return order.getId();

    }

    // 취소
    @Transactional
    public void cancelOrder(Long orderId){
        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        // 주문 취소 -> 더티체크(변경점확인) 으로 주문 테이블 뿐만 아니라 delivery, orderItem 상태까지 한 트랜잭션에서 변경
        order.cancel();

    }
    // 검색
    public List<Order> findOlders(OrderSearch orderSearch){
//        return orderRepository.findAllbyString(orderSearch);
        return orderRepository.findAll(orderSearch);
    }
}

