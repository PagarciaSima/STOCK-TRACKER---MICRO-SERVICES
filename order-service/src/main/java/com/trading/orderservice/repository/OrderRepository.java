package com.trading.orderservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trading.orderservice.entity.Order;

/**
 * Spring Data JPA repository for {@link Order} entities.
 */
public interface OrderRepository extends JpaRepository<Order, String> {

    /**
     * Finds all orders of a user ordered by creation date, newest first.
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @return the list of orders for the user (possibly empty). Never null.
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

}
