package com.utp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

/**
 * Configuración de Seguridad con JWT + Sesión Híbrido
 * 
 * PATRONES IMPLEMENTADOS:
 * - SINGLETON: Bean de configuración único para toda la aplicación
 * - FACTORY: DaoAuthenticationProvider factory para crear proveedores de autenticación
 * - STRATEGY: Diferentes estrategias de autenticación (JWT, Session, Remember-me)
 * - TEMPLATE METHOD: SecurityFilterChain define template para configuración de seguridad
 * 
 * Configura:
 * - Autenticación híbrida (JWT para API + Session para SSR)
 * - Remember Me persistente
 * - CSRF protection
 * - Autorización por roles
 * 
 * @author Julio Pariona
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private DataSource dataSource;

    /**
     * PATRÓN SINGLETON: Encoder único para toda la aplicación
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * PATRÓN FACTORY: Factory para crear el proveedor de autenticación
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    /**
     * PATRÓN SINGLETON: AuthenticationManager único
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * PATRÓN SINGLETON: Repository para tokens persistentes de Remember-Me
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

    /**
     * PATRÓN TEMPLATE METHOD: Template para configuración de seguridad
     * PATRÓN STRATEGY: Diferentes estrategias según el tipo de endpoint
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuración CSRF
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Deshabilitar CSRF para APIs
                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            
            // Configuración de sesiones (STRATEGY PATTERN)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(3)
                .maxSessionsPreventsLogin(false)
                .sessionRegistry(sessionRegistry())
            )
            
            // Configuración de autorización por endpoints
            .authorizeHttpRequests(authz -> authz
                // Recursos públicos
                .requestMatchers("/", "/home", "/products", "/products/**", 
                                "/css/**", "/js/**", "/img/**", "/assets/**", 
                                "/favicon.ico", "/error/**").permitAll()
                
                // Autenticación
                .requestMatchers("/login", "/register", "/api/auth/**").permitAll()
                
                // APIs protegidas
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/**").authenticated()
                
                // Páginas web protegidas
                .requestMatchers("/dashboard/**", "/cart/**", "/checkout/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Configuración de login (STRATEGY PATTERN)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("email")
                .passwordParameter("password")
                .permitAll()
            )
            
            // Configuración de logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/home")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )
            
            // Remember Me persistente
            .rememberMe(rememberMe -> rememberMe
                .key("uniqueAndSecret")
                .tokenRepository(persistentTokenRepository())
                .userDetailsService(userDetailsService)
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 días
                .rememberMeParameter("remember-me")
            )
            
            // Configuración de excepciones
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedPage("/error/403")
            );

        // Agregar filtro JWT antes del filtro de autenticación estándar
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * PATRÓN SINGLETON: Registry de sesiones único
     */
    @Bean
    public org.springframework.security.core.session.SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }
}