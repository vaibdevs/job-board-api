package com.jobboard.config;

import com.jobboard.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/jobs", "/jobs/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/companies/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/files/{filename}").permitAll()

                        // JOBSEEKER endpoints
                        .requestMatchers(HttpMethod.POST, "/jobs/{id}/apply").hasRole("JOBSEEKER")
                        .requestMatchers(HttpMethod.GET, "/applications/mine").hasRole("JOBSEEKER")
                        .requestMatchers(HttpMethod.DELETE, "/applications/{id}").hasRole("JOBSEEKER")

                        // EMPLOYER endpoints
                        .requestMatchers(HttpMethod.POST, "/companies").hasRole("EMPLOYER")
                        .requestMatchers(HttpMethod.POST, "/jobs").hasRole("EMPLOYER")
                        .requestMatchers(HttpMethod.PUT, "/jobs/{id}").hasRole("EMPLOYER")
                        .requestMatchers(HttpMethod.DELETE, "/jobs/{id}").hasRole("EMPLOYER")
                        .requestMatchers(HttpMethod.GET, "/jobs/{id}/applications").hasRole("EMPLOYER")
                        .requestMatchers(HttpMethod.PATCH, "/applications/{id}/status").hasRole("EMPLOYER")

                        // ADMIN endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
