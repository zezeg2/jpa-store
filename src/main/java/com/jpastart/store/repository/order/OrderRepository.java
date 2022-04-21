package com.jpastart.store.repository.order;

import com.jpastart.store.domain.order.dto.SimpleOrderQueryDto;
import com.jpastart.store.domain.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class OrderRepository {
    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAllbyString(OrderSearch orderSearch) {

        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.orderStatus = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class).setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /**
     * JPA Criteria
     */
    public List<Object> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Object> query = criteriaBuilder.createQuery();
        Root<Order> order = query.from(Order.class);
        Join<Object, Object> member = order.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = criteriaBuilder.equal(order.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    criteriaBuilder.like(member.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);

        }
        query.where(criteriaBuilder.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Object> lastQuery = em.createQuery(query).setMaxResults(1000); //최대 1000건
        return lastQuery.getResultList();


    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d", Order.class)
                .getResultList();
    }

//    public List<SimpleOrderQueryDto> findOrderDto() {
//        return em.createQuery("select new com.jpastart.store.domain.order.dto.SimpleOrderQueryDto(o.id, m.name, o.orderDateTime, o.orderStatus, d.address) " +
//                "from Order o " +
//                "join o.member m " +
//                "join o.delivery d",SimpleOrderQueryDto.class).getResultList();
//    }

    public List<Order> findAllWithItem() {
        // distinct 는 JPA 에서 조회쿼리를 날렸을 떄 조회된 값이 같은 객체를 가리키고 있으면 중복을 제거하고 하나의 데이터만 넘겨준다.
        return em.createQuery("select distinct o from Order o " +
                "join fetch o.member m " +
                "join fetch o.delivery d " +
                "join fetch  o.orderItems oi " +
                "join fetch oi.item i", Order.class)
                .getResultList();
    }
}
