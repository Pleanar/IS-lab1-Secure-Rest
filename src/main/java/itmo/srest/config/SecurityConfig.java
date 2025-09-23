package itmo.srest.config;

import itmo.srest.filter.JwtFilter;
import itmo.srest.filter.XssFilter;
import itmo.srest.handler.CustomAccessDeniedHandler;
import itmo.srest.handler.CustomLogoutHandler;
import itmo.srest.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final XssFilter xssFilter;

    private final JwtFilter jwtFilter;

    private final UserService userService;

    private final CustomAccessDeniedHandler accessDeniedHandler;

    private final CustomLogoutHandler customLogoutHandler;

    public SecurityConfig(XssFilter xssFilter,
                          JwtFilter jwtFilter,
                          UserService userService,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          CustomLogoutHandler customLogoutHandler) {
        this.xssFilter = xssFilter;
        this.jwtFilter = jwtFilter;
        this.userService = userService;
        this.accessDeniedHandler = accessDeniedHandler;
        this.customLogoutHandler = customLogoutHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/auth/registration", "/auth/login", "/auth/refresh_token").permitAll();
            auth.anyRequest().authenticated();
        }).userDetailsService(userService).exceptionHandling(e -> {
            e.accessDeniedHandler(accessDeniedHandler);
            e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
        })
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(xssFilter, JwtFilter.class)
        .logout(log -> {
            log.logoutUrl("/logout");
            log.addLogoutHandler(customLogoutHandler);
            log.logoutSuccessHandler(((request, response, authentication) ->
                    SecurityContextHolder.clearContext()));
        });

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

