package com.jpastart.store;

import com.jpastart.store.domain.address.Address;
import com.jpastart.store.domain.delivery.Delivery;
import com.jpastart.store.domain.item.Book;
import com.jpastart.store.domain.member.entity.Member;
import com.jpastart.store.domain.order.entity.Order;
import com.jpastart.store.domain.orderitem.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class InitDB {

    private final InitService initService;

    @PostConstruct
    public void init(){
        initService.dbInit1();
        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;
        public void dbInit1(){
            Member member = createMember("userA", new Address("seoul", "dongjak-gu 31-7", "12720"));

            em.persist(member);

            Book book1 = createBook("Spring JPA Book1", 10000, 1000);

            Book book2 = createBook("Spring JPA Book2", 20000, 1000);

            em.persist(book1);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 2);

            Delivery delivery = createDelivery(member);

            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }


        public void dbInit2(){
            Member member = createMember("userB", new Address("seoul", "dongjak-gu 32-7", "12720"));

            em.persist(member);

            Book book1 = createBook("Spring Advanced Book1", 15000, 1000);

            Book book2 = createBook("Spring Advanced Book2", 30000, 1000);

            em.persist(book1);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 4);

            Delivery delivery = createDelivery(member);

            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        private Book createBook(String name, int price, int quantity) {
            Book book1 = new Book();
            book1.setName(name);
            book1.setPrice(price);
            book1.setStockQuantity(quantity);
            return book1;
        }

        private Member createMember(String name, Address address) {
            Member member = new Member();
            member.setName(name);
            member.setAddress(address);
            return member;
        }

        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }
    }
}


