package com.finanzas.app.categories.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.finanzas.app.auth.domain.AuthProvider;
import com.finanzas.app.auth.domain.Role;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.infrastructure.persistence.UserJpaRepository;
import com.finanzas.app.categories.domain.Category;
import com.finanzas.app.config.JpaAuditingConfig;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class CategoryJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    private User persistedUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed-password");
        user.setFullName("Jane Doe");
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setRole(Role.USER);
        return userJpaRepository.saveAndFlush(user);
    }

    private Category category(String name, boolean isDefault, User owner) {
        Category category = new Category();
        category.setName(name);
        category.setDefault(isDefault);
        category.setUser(owner);
        return category;
    }

    @Test
    void findsGlobalAndOwnCategoriesButNotOtherUsersCustomOnes() {
        User owner = persistedUser("owner@example.com");
        User other = persistedUser("other@example.com");
        categoryJpaRepository.saveAndFlush(category("Comida", true, null));
        categoryJpaRepository.saveAndFlush(category("Mascotas", false, owner));
        categoryJpaRepository.saveAndFlush(category("Viajes", false, other));

        var visible = categoryJpaRepository.findVisibleTo(owner.getId());

        assertThat(visible).extracting(Category::getName).containsExactlyInAnyOrder("Comida", "Mascotas");
    }

    @Test
    void detectsExistingNameCaseInsensitiveAmongVisibleCategories() {
        User owner = persistedUser("owner@example.com");
        categoryJpaRepository.saveAndFlush(category("Comida", true, null));

        assertThat(categoryJpaRepository.existsVisibleToByNameIgnoreCase(owner.getId(), "comida")).isTrue();
        assertThat(categoryJpaRepository.existsVisibleToByNameIgnoreCase(owner.getId(), "Gasolina")).isFalse();
    }
}
