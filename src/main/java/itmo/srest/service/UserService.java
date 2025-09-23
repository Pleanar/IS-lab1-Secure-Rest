package itmo.srest.service;

import itmo.srest.entity.Role;
import itmo.srest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    boolean existsByUsername(String username);

    Page<User> findAll(Pageable pageable);

    void deleteByUsername(String username);

    Page<User> findAllExcludeRole(Role role, Pageable pageable);
}
