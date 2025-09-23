package itmo.srest.controller;

import itmo.srest.entity.Role;
import itmo.srest.entity.User;
import itmo.srest.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
public class DataController {
    private final UserService userService;

    public DataController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(Pageable pageable, Authentication authentication) {
        UserDetails userDetails = userService.loadUserByUsername(authentication.getName());
        User currentUser = (User) userDetails;

        Page<User> page;
        if (currentUser.getRole() == Role.ADMIN) {
            page = userService.findAll(pageable);
        } else {
            page = userService.findAllExcludeRole(Role.ADMIN, pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("users", page.stream()
                .map(user -> {
                    String info = user.getUsername();
                    if (currentUser.getRole() == Role.ADMIN) info += " : " + user.getRole();
                    return info;
                })
                .toList());
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());

        return ResponseEntity.ok(response);
    }

}
