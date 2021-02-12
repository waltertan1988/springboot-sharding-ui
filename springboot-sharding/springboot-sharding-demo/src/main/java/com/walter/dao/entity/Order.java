package com.walter.dao.entity;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Data
@Entity
@Table(name = "T_ORDER")
@DynamicUpdate
@ToString(callSuper = true)
public class Order {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ORDER_ID", unique = true, nullable = false)
    private long orderId;

    @Column(name = "USER_ID", nullable = false)
    private long userId;

    @Column(name = "STATUS", nullable = false)
    private String status;

}
