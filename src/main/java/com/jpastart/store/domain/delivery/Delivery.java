package com.jpastart.store.domain.delivery;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jpastart.store.domain.address.Address;
import com.jpastart.store.domain.order.entity.Order;
import com.jpastart.store.domain.status.DeliveryStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@Setter
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = LAZY)
    @JsonIgnore
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;
}
