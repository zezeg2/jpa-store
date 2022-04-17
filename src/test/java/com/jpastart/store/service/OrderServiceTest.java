package com.jpastart.store.service;

import com.jpastart.store.domain.address.Address;
import com.jpastart.store.domain.item.Book;
import com.jpastart.store.domain.item.Item;
import com.jpastart.store.domain.member.Member;
import com.jpastart.store.domain.order.Order;
import com.jpastart.store.domain.status.OrderStatus;
import com.jpastart.store.exception.NotEnoughStockException;
import com.jpastart.store.repository.ItemRepository;
import com.jpastart.store.repository.MemberRepository;
import com.jpastart.store.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @PersistenceContext
    EntityManager em;
    @Autowired OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ItemRepository itemRepository;

    @DisplayName("1.상품 주문")
    @Test
    @Rollback(value = false)
    void test_1() throws Exception {
        // given
        Member member = getMember();
        Item book = getBook("JPA Book", 10000, 20);
        int orderCount = 10;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.ORDER, getOrder.getStatus());
        assertEquals(1, getOrder.getOrderItems().size());
        assertEquals(10000 * orderCount, getOrder.getTotalPrice());
        assertEquals(10, book.getStockQuantity());

    }


    @DisplayName("2.상품주문_재고수량 초과")
    @Test
    void test_2()  throws Exception{
        // given
        Member member = getMember();
        Item book = getBook("JPA Book", 10000, 20);
        int orderCount = 22;

        // when

        // then
        NotEnoughStockException thrown = assertThrows(NotEnoughStockException.class, ()-> orderService.order(member.getId(), book.getId(), orderCount));
        assertEquals("not enought stock", thrown.getMessage());
    }

    @DisplayName("3.주문 취소")
    @Test
    void test_3() {
        // given
        Member member = getMember();
        Item book = getBook("JPA Book", 10000, 20);
        int orderCount = 5;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // when
        orderService.cancelOrder(orderId);

        // then
        assertEquals(OrderStatus.CANCEL, orderRepository.findOne(orderId).getStatus());
        assertEquals(20, book.getStockQuantity());
    }

    private Item getBook(String name, int price, int quantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }

    private Member getMember() {
        Member member = new Member();
        member.setName("henry");
        member.setAddress(new Address("서울", "동작구 상도동 성대로", "12345"));
        em.persist(member);
        return member;
    }
}
