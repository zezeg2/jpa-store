package com.jpastart.store.api;

import com.jpastart.store.domain.order.dto.SimpleOrderDto;
import com.jpastart.store.domain.order.dto.SimpleOrderQueryDto;
import com.jpastart.store.domain.order.entity.Order;
import com.jpastart.store.repository.order.OrderDtoQueryRepository;
import com.jpastart.store.repository.order.OrderRepository;
import com.jpastart.store.repository.order.OrderSearch;
import com.jpastart.store.repository.order.SimpleOrderDtoQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * X To One 성능 최지적화
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    private final SimpleOrderDtoQueryRepository simpleOrderDtoQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllbyString(new OrderSearch());
        /* Hibernate5Module Feature.FORCE_LAZY_LOADING 사용하지 않고 Lazy 강제 초기화, order.getMember()까지는 프록시 객체상태, */
        for (Order order : all){
            order.getMember().getName();
            order.getDelivery().getAddress();
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){

        /**
         * 데이터 2개 조회
         * N + 1 -> 1 + N(Member 2) + N(Member 2)
         */
        List<Order> orders = orderRepository.findAllbyString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> collect = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
        return collect;

    }

    @GetMapping("/api/v4/simple-orders")
    public List<SimpleOrderQueryDto> ordersV4(){
        return simpleOrderDtoQueryRepository.findSimpleOrderDto();
    }
}
