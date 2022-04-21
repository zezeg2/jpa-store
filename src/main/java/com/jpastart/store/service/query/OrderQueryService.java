package com.jpastart.store.service.query;

import com.jpastart.store.domain.order.dto.OrderDto;
import com.jpastart.store.domain.order.entity.Order;
import com.jpastart.store.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class OrderQueryService {

    private final OrderRepository orderRepository;


    public List<OrderDto> getOrdersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> collect = orders.stream()
                .map(OrderDto::new)
                .collect(toList());
        return collect;
    }


}
