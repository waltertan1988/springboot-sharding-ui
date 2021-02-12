package com.walter.dao.entity;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "T_ORDER_ITEM")
@DynamicUpdate
@ToString(callSuper = true)
public class OrderItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ORDER_ITEM_ID", unique = true, nullable = false)
    private long orderItemId;

    @Column(name = "ORDER_ID", nullable = false)
    private long orderId;

    @Column(name = "USER_ID", nullable = false)
    private long userId;
}
