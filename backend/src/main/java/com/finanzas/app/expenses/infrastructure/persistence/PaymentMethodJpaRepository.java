package com.finanzas.app.expenses.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.finanzas.app.expenses.domain.PaymentMethod;

public interface PaymentMethodJpaRepository extends JpaRepository<PaymentMethod, Long> {

    @Query("select p from PaymentMethod p where p.user is null or p.user.id = :userId")
    List<PaymentMethod> findVisibleTo(@Param("userId") Long userId);

    @Query("select p from PaymentMethod p where (p.user is null or p.user.id = :userId) and p.id = :paymentMethodId")
    Optional<PaymentMethod> findVisibleToById(@Param("userId") Long userId, @Param("paymentMethodId") Long paymentMethodId);

    @Query("select count(p) > 0 from PaymentMethod p "
            + "where (p.user is null or p.user.id = :userId) and lower(p.name) = lower(:name)")
    boolean existsVisibleToByNameIgnoreCase(@Param("userId") Long userId, @Param("name") String name);
}
