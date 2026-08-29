package com.finanzas.app.expenses.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.finanzas.app.auth.domain.AuthProvider;
import com.finanzas.app.auth.domain.Role;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.infrastructure.persistence.UserJpaRepository;
import com.finanzas.app.config.JpaAuditingConfig;
import com.finanzas.app.expenses.domain.PaymentMethod;
import com.finanzas.app.expenses.domain.PaymentMethodType;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class PaymentMethodJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PaymentMethodJpaRepository paymentMethodJpaRepository;

    private User persistedUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed-password");
        user.setFullName("Jane Doe");
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setRole(Role.USER);
        return userJpaRepository.saveAndFlush(user);
    }

    private PaymentMethod method(String name, PaymentMethodType type, boolean isDefault, User owner) {
        PaymentMethod method = new PaymentMethod();
        method.setName(name);
        method.setType(type);
        method.setDefault(isDefault);
        method.setUser(owner);
        return method;
    }

    @Test
    void findsGlobalAndOwnMethodsButNotOtherUsersCustomOnes() {
        User owner = persistedUser("owner@example.com");
        User other = persistedUser("other@example.com");
        paymentMethodJpaRepository.saveAndFlush(method("Efectivo", PaymentMethodType.CASH, true, null));
        paymentMethodJpaRepository.saveAndFlush(method("Nequi", PaymentMethodType.OTHER, false, owner));
        paymentMethodJpaRepository.saveAndFlush(method("Daviplata", PaymentMethodType.OTHER, false, other));

        var visible = paymentMethodJpaRepository.findVisibleTo(owner.getId());

        assertThat(visible).extracting(PaymentMethod::getName).containsExactlyInAnyOrder("Efectivo", "Nequi");
    }

    @Test
    void findsVisibleMethodByIdButNotAnotherUsersCustomMethod() {
        User owner = persistedUser("owner@example.com");
        User other = persistedUser("other@example.com");
        PaymentMethod global = paymentMethodJpaRepository.saveAndFlush(method("Efectivo", PaymentMethodType.CASH, true, null));
        PaymentMethod ownedByOther = paymentMethodJpaRepository.saveAndFlush(
                method("Daviplata", PaymentMethodType.OTHER, false, other));

        assertThat(paymentMethodJpaRepository.findVisibleToById(owner.getId(), global.getId())).isPresent();
        assertThat(paymentMethodJpaRepository.findVisibleToById(owner.getId(), ownedByOther.getId())).isEmpty();
    }

    @Test
    void detectsExistingNameCaseInsensitiveAmongVisibleMethods() {
        User owner = persistedUser("owner@example.com");
        paymentMethodJpaRepository.saveAndFlush(method("Efectivo", PaymentMethodType.CASH, true, null));

        assertThat(paymentMethodJpaRepository.existsVisibleToByNameIgnoreCase(owner.getId(), "efectivo")).isTrue();
        assertThat(paymentMethodJpaRepository.existsVisibleToByNameIgnoreCase(owner.getId(), "Nequi")).isFalse();
    }
}
