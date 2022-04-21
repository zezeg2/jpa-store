package com.jpastart.store.repository.order;

import com.jpastart.store.domain.order.dto.OrderFlatDto;
import com.jpastart.store.domain.order.dto.OrderQueryDto;
import com.jpastart.store.domain.orderitem.dto.OrderItemQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderDtoQueryRepository {
    private final EntityManager em;

    public List<OrderQueryDto> findOrderDto() {
        List<OrderQueryDto> orders = findOrders();

        orders.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return orders;
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery("select new com.jpastart.store.domain.order.dto.OrderQueryDto(o.id, m.name, o.orderDateTime, o.orderStatus, d.address) " +
                "from Order o " +
                "join o.member m " +
                "join o.delivery d", OrderQueryDto.class).getResultList();
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery("select new com.jpastart.store.domain.orderitem.dto.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                        "from OrderItem oi " +
                        "join oi.item i " +
                        "where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public List<OrderQueryDto> findAllByDtoOptimization() {
        List<OrderQueryDto> orders = findOrders();
        List<Long> orderIds = toOrderIds(orders);
        List<OrderItemQueryDto> orderItems = findOrderItemMap(orderIds);

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));

        orders.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return orders;


    }

    private List<OrderItemQueryDto> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery("select new com.jpastart.store.domain.orderitem.dto.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                        "from OrderItem oi " +
                        "join oi.item i " +
                        "where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
        return orderItems;
    }

    private List<Long> toOrderIds(List<OrderQueryDto> orders) {
        List<Long> orderIds = orders.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    public List<OrderFlatDto> findAllByDtoFlat() {
        return em.createQuery("select new com.jpastart.store.domain.order.dto.OrderFlatDto(o.id, m.name, o.orderDateTime, o.orderStatus, d.address, i.name, oi.orderPrice, oi.count) " +
                "from Order o " +
                "join o.member m " +
                "join o.delivery d " +
                "join o.orderItems oi j" +
                "oin oi.item i" , OrderFlatDto.class).getResultList();
    }
}
