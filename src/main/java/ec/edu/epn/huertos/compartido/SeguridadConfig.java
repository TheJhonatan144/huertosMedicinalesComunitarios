package ec.edu.epn.huertos.compartido;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Primera línea de control de acceso (por URL y rol). La autorización por comunidad y recurso
 * se repite en los servicios; ocultar un botón nunca sustituye estas comprobaciones.
 */
@Configuration
public class SeguridadConfig {

    @Bean
    SecurityFilterChain cadenaDeFiltros(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/mapa", "/ranking", "/comunidades/**", "/api/publico/**",
                                "/login", "/error", "/css/**", "/js/**", "/vendor/**", "/favicon.ico",
                                "/actuator/health/**").permitAll()
                        .requestMatchers("/gestor/**").hasRole("GESTOR_HUERTO")
                        .requestMatchers("/participante/**").hasRole("PARTICIPANTE")
                        .requestMatchers("/revision/**").hasRole("ADMIN_EVIDENCIAS")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/panel", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?sesionCerrada"))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; img-src 'self' data: https://*.tile.openstreetmap.org; "
                                        + "style-src 'self' 'unsafe-inline'; script-src 'self'; "
                                        + "frame-ancestors 'none'; form-action 'self'"))
                        .referrerPolicy(ref -> ref.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
                                        .ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
