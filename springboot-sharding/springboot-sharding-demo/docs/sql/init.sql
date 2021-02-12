
#### DS0
CREATE TABLE `sharding0`.`t_order0` (
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    `status` varchar(255) COLLATE utf8_bin NOT NULL,
    PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `sharding0`.`t_order1` (
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    `status` varchar(255) COLLATE utf8_bin NOT NULL,
    PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `sharding0`.`t_order_item0` (
    `order_item_id` bigint(20) NOT NULL,
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    PRIMARY KEY (`order_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `sharding0`.`t_order_item1` (
    `order_item_id` bigint(20) NOT NULL,
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    PRIMARY KEY (`order_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

#### DS1
CREATE TABLE `sharding1`.`t_order0` (
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    `status` varchar(255) COLLATE utf8_bin NOT NULL,
    PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `sharding1`.`t_order1` (
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    `status` varchar(255) COLLATE utf8_bin NOT NULL,
    PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `sharding1`.`t_order_item0` (
    `order_item_id` bigint(20) NOT NULL,
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    PRIMARY KEY (`order_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `sharding1`.`t_order_item1` (
    `order_item_id` bigint(20) NOT NULL,
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    PRIMARY KEY (`order_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

insert into `sharding0`.`t_order0` (`order_id`, `user_id`, `status`) values('4740584156199727104','12558','NEW');
insert into `sharding0`.`t_order_item0` (`order_item_id`, `order_id`, `user_id`) values('2','4740584156199727104','12558');

insert into `sharding1`.`t_order1` (`order_id`, `user_id`, `status`) values('4740584156199727105','9785','PENDING');
insert into `sharding1`.`t_order_item1` (`order_item_id`, `order_id`, `user_id`) values('1','4740584156199727105','9785');
