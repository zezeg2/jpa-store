## 양방향 연관관계 문제 해결 및 성능 최적화

## 지연로딩과 조회 성능 최적화(xToOne)

**지연로딩으로 발생하는 성능 문제를 단계적으로 해결**

### CASE1 → Entity 직접 노출 (비권장)

- problem 1 : json 데이터 생성시 순환참조로 인한 문제

	- 양방향 연관관계에서 한쪽에서는 `@JsonIgnore` 어노테이션으로 순환참조를 끊어줘야한다.

- problem2 : 양방향 연관관계에서 지연로딩(fetch=LAZY)인경우 즉시 조회하지 않은 데이터(Entity)에 프록시가 존재한다.

	- jackson 라이브러리는 이 프록시 객체를 json으로 어떻게 생성하는지 알 수없음

		→따라서 json 생성시 Entity 대한 정보를 가져오지 않고 예외 발생

		→ `Hibernage5Module` 을 스프링 bean으로 등록하여 해결

		```java
		// 로딩되지 않은 Entity는 null
		@Bean
		    Hibernate5Module hibernate5Module(){
		        return new Hibernate5Module();
		    }
		
		// 강제로 지연로딩 실행
		@Bean
		Hibernate5Module hibernate5Module(){
		   Hibernate5Module hibernate5Module = new Hibernate5Module();
		    hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		    return hibernate5Module;
		}
		```

		```json
		// 로딩되지 않은 Entity는 null
		[
		{
		"id": 1,
		"member": null,
		"delivery": null,
		"orderDateTime": "2022-04-18T21:35:50.16961",
		"orderStatus": "ORDER",
		"totalPrice": 50000
		},
		{
		"id": 2,
		"member": null,
		"delivery": null,
		"orderDateTime": "2022-04-18T21:35:50.184529",
		"orderStatus": "ORDER",
		"totalPrice": 165000
		}
		]
		
		// 강제로 지연로딩 실행
		[
		{
		"id": 1,
		"member":{
		"id": 1,
		"name": "userA",
		"address":{
		"city": "seoul",
		"street": "dongjak-gu 31-7",
		"zipcode": "12720"
		}
		},
		"delivery":{
		"id": 1,
		"address":{
		"city": "seoul",
		"street": "dongjak-gu 31-7",
		"zipcode": "12720"
		},
		"deliveryStatus": null
		},
		"orderDateTime": "2022-04-18T21:45:59.105966",
		"orderStatus": "ORDER",
		"totalPrice": 50000
		},
		...
		]
		```

		> 참고:  간단한 애플리케이션이 아니면 Entity를 API 응답으로 외부로 노출하는 것은 좋지 않다. →  Hibernate5Module 를 사용하기 보다는 DTO로 변환해서 반환하는 것이 더 좋은 방법이다.

		> 주의: 지연 로딩(LAZY)을 피하기 위해 즉시 로딩(EARGR)으로 설정하면 안된다 즉시 로딩 때문에 연관관계가 필요 없는 경우에도 데이터를 항상 조회하게 된다 →  성능 문제가 발생할 수 있다. 즉시 로딩으로설정하면 성능 튜닝이 매우 어려워 진다. → 항상 지연 로딩을 기본으로,  성능 최적화가 필요한 경우에는 페치 조인(fetch join)을 사용

### CASE2 → Entity**를 DTO로 변환**

```java
@Data
public class SimpleOrderDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public SimpleOrderDto(Order order) {
        orderId = order.getId();
        name = order.getMember().getName();
        orderDate = order.getOrderDateTime();
        orderStatus = order.getOrderStatus();
        address = order.getDelivery().getAddress();

    }
}
```

- Entity 를 DTO로 변환하는 일반적인 방법

	- 쿼리가 총 1 + N + N번 실행된다. (CASE1과 쿼리수 결과는 같다.)

		- order 조회 1번(order 조회 결과 수가 N이 된다.)

		- order → member 지연 로딩 조회 N 번

		- order → delivery 지연 로딩 조회 N 번

			> N + 1 문제 : 첫번째(1st query) 로 조회된 데이터가 N개이고 M번의 LAZY 로딩을 초기화 할 경우 →  order의 결과가 4개이고 order와 연관관계 매핑 되어있는 Entity가 2개이면  최악의 경우 쿼리가 (최악의 경우)1 + 4 + 4번 실행된다.

			참고 : 지연로딩은 영속성 컨텍스트에서 조회하므로, 이미 조회된 경우 쿼리를 생략한다.

			> 

### CASE3 →  **페치 조인 (Fetch Join) 최적화**

- Repository에  페치조인을 위한 쿼리를 추가한다 (함께 조회하고자 하는 Entity를 패치조인 한다)

	```java
	public List<Order> findAllWithMemberDelivery() {
	        return em.createQuery(
	                "select o from Order o " +
	                        "join fetch o.member m " +
	                        "join fetch o.delivery d", Order.class)
	                .getResultList();
	}
	```

	- Entity를 페치 조인(fetch join)을 사용해서 쿼리 1번에 조회한다.
	- 페치 조인으로 order → member , order → delivery 는 이미 조회 된 상태 이므로 지연로딩이 일어나지 않음

### !! 페치조인 VS 일반 조인

- 일반 Join
	- Fetch Join과 달리 연관 Entity에 Join을 걸어도 실제 쿼리에서 SELECT 하는 Entity는**오직 JPQL에서 조회하는 주체가 되는 Entity만 조회하여 영속화**
	- 조회의 주체가 되는 Entity만 SELECT 해서 영속화하기 때문에 데이터는 필요하지 않지만 연관 Entity가 검색조건에는 필요한 경우에 주로 사용됨
- Fetch Join
	- 조회의 주체가 되는 Entity 외에 Fetch Join이 걸린 연관 Entity도 함께 SELECT 하여 **모두 영속화**
	- Fetch Join이 걸린 Entity 모두 영속화하기 때문에 FetchType이 Lazy인 Entity를 참조하더라도이미 영속성 컨텍스트에 들어있기 때문에 따로 쿼리가 실행되지 않은 채로 N+1 문제가 해결됨

### CASE4 → JPA에서 DTO로 바로 조회

Repository 에서 Entity의 특정 컬럼을  DTO의 속성값을 직접 매핑하기 위한 쿼리를 추가한다

- Entity 전체 속성에 대해 조회 하는것이 아닌 DTO로 전달하고자 하는 속성만 조회하도록 전용 DTO를 생성한다

	```java
	/* 쿼리 전용 DTO */
	@Data
	@AllArgsConstructor
	public class SimpleOrderQueryDto {
	    private Long orderId;
	    private String name;
	    private LocalDateTime orderDate;
	    private OrderStatus orderStatus;
	    private Address address; 
	}
	```

- 이 때 생성자에 Entity 자체를 넣는것이 아닌 조회하고자 하는 컬럼만 입력하여 생성자를 만듬(`AllArgsConstructor` 사용)

	- 일반적인 SQL을 사용할 때 처럼 원하는 값을 선택해서 조회

	- new 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환

	- SELECT 절에서 원하는 데이터를 직접 선택하므로 DB 애플리케이션 네트웍 용량 최적화

	- Repository 재사용성 떨어짐, API 스펙에 맞춘 코드가 Repository에 들어가는 단점

		> API스펙에 따른 로직이 Repository에 들어가는것은 바람직하지 않다. Repository는 Entity를 조회하는 용도로만 사용하는것이 바람직함 따라서 CASE4를 고려해야 한다면 API 스팩 전용 리포지토리를 생성해서 구분하는것도 하나의 방법이 될 수 있다.

	```java
	// DTO 클래스 입력시 Reference ClassName을 입력해 줘야한다.
	public List<SimpleOrderQueryDto> findOrderDto() {
	    return em.createQuery("select new com.jpastart.store.domain.order.dto.SimpleOrderQueryDto(o.id, m.name, o.orderDateTime, o.orderStatus, d.address) " +
	            "from Order o " +
	            "join o.member m " +
	            "join o.delivery d",SimpleOrderQueryDto.class).getResultList();
	}
	```

- 쿼리 트래픽은 줄어드나(성능최적화 면에 있어서는 CASE 3 보다 조금 우위), Repository 재사용성이 매우 떨어진다

‼️ **정리**

Entity를 DTO로 변환하거나, DTO로 바로 조회하는 두가지 방법은 각각 장단점이 있다. 둘중 상황에 따라서 더 나은 방법을 선택하면 된다. Entity로 조회하면 Repository 재사용성도 좋고, 개발도 단순해진다.

**쿼리 방식 선택 권장 순서**

1. 우선 Entity를 DTO로 변환하는 방법을 선택한다.
2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다.
3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.

## 컬렉션 조회 최적화(xtoMany)

### CASE1 → Entity 직접 노출

- orderItem , item 관계를 직접 초기화하면 `Hibernate5Module` 설정에 의해 Entity(프록시 객체)를 JSON으로 생성한다

	```java
	@Bean
	Hibernate5Module hibernate5Module(){
	    return new Hibernate5Module();
	}
	```

- 양방향 연관관계에서 순환참조 무한 루프에 걸리지 않게 한곳에 `@JsonIgnore` 를 추가해야 한다.

	```java
	@JsonIgnore
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "order_id")
	private Order order;
	```

- 엔티티를 직접 노출하므로 좋은 방법은 아니다.

### CASE2 → Entity를 **DTO로 변환**

- Collection → orderItem (N) - order(1) 관계에서 두 Entity 모두 DTO로 변환해준다

	- OrderDto

		```java
		public class OrderDto {
		
		    private Long orderId;
		
		    private String name;
		
		    private LocalDateTime orderDate;
		
		    private OrderStatus orderStatus;
		
		    private Address address;
		//    private List<OrderItem> orderItems;
		private List<OrderItemDto> orderItems;
		
		    public OrderDto(Order order) {
		        orderId = order.getId();
		        name = order.getMember().getName();
		        orderDate = order.getOrderDateTime();
		        orderStatus = order.getOrderStatus();
		        address = order.getDelivery().getAddress();
		//        order.getOrderItems().forEach(o -> o.getItem().getName()); //  Order DTO내부에서 OrderItem Entity가 노출됨
		//        orderitems = order.getOrderItems();
		orderItems = order.getOrderItems().stream()
		                .map(OrderItemDto::new)
		                .collect(Collectors.toList());
		
		    }
		}
		```

	- OrderItemDto

		```java
		public class OrderItemDto {
		
		    private String itemName;
		
		    private int orderPrice;
		
		    private int count;
		
		    public OrderItemDto(OrderItem orderItem) {
		
		        itemName = orderItem.getItem().getName();
		
		        orderPrice = orderItem.getOrderPrice();
		
		        count = orderItem.getCount();
		    }
		}
		```

		```java
		@GetMapping("/api/v3/simple-orders")
		public List<SimpleOrderDto> ordersV3(){
		    List<Order> orders = orderRepository.findAllWithMemberDelivery();
		    List<SimpleOrderDto> collect = orders.stream()
		            .map(SimpleOrderDto::new)
		            .collect(Collectors.toList());
		    return collect;
		
		}
		```

	- 총 조회 쿼리 횟수

		- order → 1
		- member → N(order 조회 횟수만큼)
		- delivery → N(order 조회 횟수만큼)
		- orderItem → N(order 조회 횟수만큼)
		- item → (orderItem 조회 횟수만큼)

### CASE3 → Entity를 **DTO로 변환 - 페치조인 최적화**

- 페치조인 쿼리를 위해 Repository에 다음 쿼리메소드를 추가한다.

	```java
	public List<Order> findAllWithItem() {
	// distinct는 JPA에서 조회쿼리를 날렸을 떄 조회된 값이 같은 객체를 가리키고 있으면 중복을 제거하고 하나의 데이터만 넘겨준다.
	return em.createQuery("select distinct o from Order o " +
	            "join fetch o.member m " +
	            "join fetch o.delivery d " +
	            "join fetch  o.orderItems oi " +
	            "join fetch oi.item i", Order.class)
	            .getResultList();
	}
	```

	```java
	@GetMapping("/api/v3/orders")
	public List<OrderDto> ordersV3(){
	    List<Order> orders = orderRepository.findAllWithItem();
	    List<OrderDto> collect = orders.stream()
	            .map(OrderDto::new)
	            .collect(Collectors.toList());
	
	    return collect;
	}
	```

	- 패치조인으로 조회 쿼리가 한번만 실행된다

	> ❓distinct 를 사용한 이유: 1대다 조인이 있으므로 데이터베이스 row가 증가한다. 그 결과 같은 order Entity의 조회 수도 증가하게 된다. JPA의 distinct는 SQL에 distinct를 추가하고, 더해서 같은 Entity가 조회되면, 애플리케이션에서 중복을 걸러준다. 이 예에서 order가 컬렉션 페치 조인 때문에 중복 조회 되는 것을 막아준다.

	> ❗참고 : 컬렉션 페치 조인을 사용하면 페이징이 불가능하다. 하이버네이트는 경고 로그를 남기면서 모든 데이터를 DB에서 읽어오고, 메모리에서 페이징 해버린다(매우 위험)

	> ❗참고 : 컬렉션 페치 조인은 1개만 사용할 수 있다. 컬렉션 둘 이상에 페치 조인을 사용하면 안된다. 데이터가 부정합하게 조회될 수 있다. 자세한 내용은 자바 ORM 표준 JPA 프로그래밍을 참고하자.

### CASE4 → Entity를 DTO로 변환 - Paging 한계 돌파

- 컬렉션을 페치 조인하면 페이징이 불가능하다.

	- 컬렉션을 페치 조인하면 일대다 조인이 발생하므로 데이터가 예측할 수 없이 증가한다.
	- 일다대에서 일(1)을 기준으로 페이징을 하는 것이 목적이다. 그런데 데이터는 다(N)를 기준으로 row가 생성된다.
	- Order를 기준으로 페이징 하고 싶은데, 다(N)인 OrderItem을 조인하면 OrderItem이 기준이 되어버린다.

- 이 경우 하이버네이트는 경고 로그를 남기고 모든 DB 데이터를 읽어서 메모리에서 페이징을 시도한다. (최악의 경우 장애로 이어질 수 있다. OOM)

	### 한계돌파 ❗

	- 우선 xToOne 관계를 모두 패치조인 한다. → ToOne 관계는 row 를 증가시키지 않으므로 페이징 쿼리에 영향 X

	- Collection 은 지연로딩으로 조회

	- 지연 로딩 최적화를 위해 `application.yml` 에 `hibernate.default_batch_fetch_size` 배치 사이즈를 설정하거나 Entity 개별적으로 `@BatchSize` 를 설정해준다.

		- `hibernate.default_batch_fetch_size` : 글로벌 설정 (권장)

			```yaml
			spring: 
			  jpa:
					properties:
						hibernate:
							default_batch_fetch_size: 1000
			```

		- `@BatchSize` : 개별 최적화

		- 이 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size 만큼 IN 쿼리로 조회한다.

	- 장점 :

		- 쿼리 호출수가 1 + N → 1 + 1로 최적화 된다
		- 조인보다 DB 데이터 전송량이 최적화 된다.(Order와 OrderItem을 조인하면 Order가 OrderItem만큼 중복되서 조회됨)
		- 패치조인 방식도다 쿼리 호출수는 약간 증가하지만, DB 데이터 전송량이 감소
		- 페이징이 가능하다

		**→** ToOne 관계는 페치 조인해도 페이징에 영향을 주지 않는다. 따라서 ToOne 관계는 페치조인으로 쿼리 수를 줄이고 해결하고, 나머지는 hibernate.default_batch_fetch_size 로 최적화 하자.

### CASE5 → **JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화**

- OrderQueryRepository에 추가

```java
public List<OrderQueryDto> findAllByDtoOptimization() {
    List<OrderQueryDto> orders = findOrders();
    List<Long> orderIds = toOrderIds(orders);
    List<OrderItemQueryDto> orderItems = findOrderItemMap(orderIds);

		// orderId에 따라 orderItemDto 매핑
    Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
            .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));

		//
    orders.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

    return orders;

}

// 조회된 orderIds 리스트로 orderItem을 조회하는 쿼리메소드
private List<OrderItemQueryDto> findOrderItemMap(List<Long> orderIds) {
    List<OrderItemQueryDto> orderItems = em.createQuery("select new com.jpastart.store.domain.orderitem.dto.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                    "from OrderItem oi " +
                    "join oi.item i " +
                    "where oi.order.id in :orderIds", OrderItemQueryDto.class)
            .setParameter("orderIds", orderIds)
            .getResultList();
    return orderItems;
}

// 조회된 order의 id값만 리스트로 꺼내는 메소드
private List<Long> toOrderIds(List<OrderQueryDto> orders) {
    List<Long> orderIds = orders.stream()
            .map(o -> o.getOrderId())
            .collect(Collectors.toList());
    return orderIds;
}
```

- Query: 루트 1번, 컬렉션 1번
- ToOne 관계들을 먼저 조회하고, 여기서 얻은 식별자 orderId로 ToMany 관계인 OrderItem 을 한꺼번에 조회
- MAP을 사용해서 매칭 성능 향상(O(1))

## OSIV(Open Session In View)와 성능최적화

### OSIV ON

- application.yaml 에서 

	```
	spring.jpa.open-in-view: true
	```

	 (기본값 true)

	- 영속성 시작 시점부터 API 응답, 혹은 View 렌더링이 완료될때까지 영속성 컨텍스트가 유지된다.

	- 장점 :

		- View Template이나 API 컨트롤러에서 지연 로딩이 가능

			> 지연 로딩은 영속성 컨텍스트가 살아있어야 가능하고, 영속성 컨텍스트는 기본적으로 데이터베이스 커넥션을 유지한다

	- 단점 :

		- 너무 장시간 데이터베이스 커넥션 리소스를 사용하게된다.

			→ 실시간 트래픽이 중요한 애플리케이션에서는 Connection이 모자랄수 있다 → 장애발생으로 이어짐

			> eg. 컨트롤러에서 외부 API를 호출하면 외부 API 대기 시간 만큼 커넥션 리소스를 반환하지 못하고, 유지해야 한다.

### OSIV OFF

- application.yaml 에서 

	```
	spring.jpa.open-in-view: false
	```

	 (기본값 true)

	- 트랜잭션을 종료할 때 영속성 컨텍스트를 닫고, 데이터베이스 커넥션도 반환한다.

	- 커넥션 리소스를 낭비하지 않는다.

	- OSIV를 끄면 모든 지연로딩을 트랜잭션 안에서 처리해야 한다

		→  지연 로딩 코드를 트랜잭션 안으로 넣어야 한다. (서비스와 컨트롤러의 명확한 분리를 필요로 한다.)