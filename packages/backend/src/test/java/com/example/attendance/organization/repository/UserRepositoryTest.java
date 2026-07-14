package com.example.attendance.organization.repository;

import com.example.attendance.common.enums.Role;
import com.example.attendance.organization.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/test-data-clock.sql")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private static final UUID DEPT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    @DisplayName("findByEmail: 存在するメールで取得できる")
    void findByEmail_existing_returnsUser() {
        var result = userRepository.findByEmail("test@example.com");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("テストユーザ");
    }

    @Test
    @DisplayName("findByEmail: 存在しないメールはempty")
    void findByEmail_notExisting_returnsEmpty() {
        var result = userRepository.findByEmail("unknown@example.com");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail: 登録済みメールはtrue")
    void existsByEmail_registered_returnsTrue() {
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
    }

    @Test
    @DisplayName("findByActiveOrderByNameAsc: 有効ユーザのみ名前順で取得")
    void findByActive_activeUsers_returnsSorted() {
        var results = userRepository.findByActiveOrderByNameAsc(true);
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(u -> u.getActive());
    }

    @Test
    @DisplayName("CRUD: ユーザの作成・更新・削除")
    void crud_createUpdateDelete() {
        var user = User.builder()
                .email("crud@example.com")
                .passwordHash("hash")
                .name("CRUDテスト")
                .role(Role.GENERAL)
                .primaryDepartmentId(DEPT_ID)
                .build();

        var saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();

        saved.setName("更新後");
        var updated = userRepository.save(saved);
        assertThat(updated.getName()).isEqualTo("更新後");

        userRepository.delete(updated);
        assertThat(userRepository.findById(updated.getId())).isEmpty();
    }
}
