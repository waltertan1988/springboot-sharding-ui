package com.walter.dao.repository;

import com.walter.dao.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("FROM Order o LEFT JOIN OrderItem i ON o.orderId = i.orderId WHERE o.orderId IN :orderIds")
    List<?> findAllByOrderIdIn(@Param("orderIds") List<Long> orderIds);
}
