package com.jpastart.store.repository.order;

import com.jpastart.store.domain.order.dto.SimpleOrderQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SimpleOrderDtoQueryRepository {
    private final EntityManager em;

    public List<SimpleOrderQueryDto> findSimpleOrderDto() {
        return em.createQuery("select new com.jpastart.store.domain.order.dto.SimpleOrderQueryDto(o.id, m.name, o.orderDateTime, o.orderStatus, d.address) " +
                "from Order o " +
                "join o.member m " +
                "join o.delivery d",SimpleOrderQueryDto.class).getResultList();
    }
}
