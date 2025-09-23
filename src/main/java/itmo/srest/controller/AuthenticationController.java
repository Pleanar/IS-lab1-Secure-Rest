package itmo.srest.controller;

import itmo.srest.dto.AuthenticationResponseDto;
import itmo.srest.dto.LoginRequestDto;
import itmo.srest.dto.RegistrationRequestDto;
import itmo.srest.service.AuthenticationService;
import itmo.srest.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    public AuthenticationController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @PostMapping("/registration")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequestDto registrationDto, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }

        if (userService.existsByUsername(registrationDto.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is taken.");
        }

        authenticationService.register(registrationDto);
        return ResponseEntity.status(HttpStatus.OK).body("Registration successful.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDto request, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }
        return ResponseEntity.status(HttpStatus.OK).body(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<AuthenticationResponseDto> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        return authenticationService.refreshToken(request, response);
    }

    @DeleteMapping("/user")
    public ResponseEntity<?> deleteUser(@RequestParam String username, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        boolean isOwner = authentication.getName().equals(username);

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }

        if (!userService.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        userService.deleteByUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body("User deleted.");
    }

}
