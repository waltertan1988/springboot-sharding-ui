package com.walter.dao.repository;

import com.walter.dao.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByOrderIdIn(List<Long> ids);
}
