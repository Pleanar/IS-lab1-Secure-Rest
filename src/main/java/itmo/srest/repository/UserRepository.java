package itmo.srest.repository;

import itmo.srest.entity.Role;
import itmo.srest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Page<User> findAllByRoleNot(Role role, Pageable pageable);

    void deleteUserByUsername(String username);
}