package org.example.Config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.example.Service.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурация бинов авторизации
 */
@Configuration
@AllArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;

    /**
     * Создание цепочки фильтров для обработки http-запросов
     * @param http объект HttpSecurity для настройки безопасности http-запросов
     * @return собранная цепочка фильтров
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                            auth.requestMatchers("/bonus_server/register", "/bonus_server/login").permitAll();
                            auth.requestMatchers(HttpMethod.POST,
                                            "/bonus_server/accrual",
                                            "/bonus_server/deduction",
                                            "/bonus_server/cancel")
                                    .hasAuthority("ROLE_WRITE");
                            auth.requestMatchers(HttpMethod.GET,
                                            "/bonus_server/history",
                                            "/bonus_server/balance")
                                    .hasAnyAuthority("ROLE_READ", "ROLE_WRITE");
                            auth.anyRequest().authenticated();
                        }
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Создание провайдера аутентфикации для проверки логина-пароля
     * @param uds объект UserDetailsService с информацией о пользователе
     * @param encoder объект для шифрования пароля
     * @return менеджер аутентификации с одним провайдером
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService uds,
                                                       PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(uds);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }
}
