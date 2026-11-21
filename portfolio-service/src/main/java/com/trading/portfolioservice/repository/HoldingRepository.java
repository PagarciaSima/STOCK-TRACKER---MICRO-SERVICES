package com.trading.portfolioservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trading.portfolioservice.entity.Holding;

/**
 * Spring Data JPA repository for {@link Holding} entities.
 */
@Repository
public interface HoldingRepository extends JpaRepository<Holding, String> {

    /**
     * Finds all holdings of a user.
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @return the list of holdings for the user (possibly empty). Never null.
     */
    List<Holding> findByUserId(String userId);

    /**
     * Finds a specific holding by user and symbol.
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @param symbol the stock ticker symbol. Cannot be null or blank.
     * @return an {@link Optional} containing the holding if found, empty otherwise.
     */
    Optional<Holding> findByUserIdAndSymbol(String userId, String symbol);
}
