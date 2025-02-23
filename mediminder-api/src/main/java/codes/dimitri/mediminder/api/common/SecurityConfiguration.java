package codes.dimitri.mediminder.api.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        HandlerExceptionResolver handlerExceptionResolver) throws Exception {
        return http
            .authorizeHttpRequests(customizer -> customizer
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/user").anonymous()
                .requestMatchers(HttpMethod.POST, "/api/user/logout/success").anonymous()
                .requestMatchers(HttpMethod.POST, "/api/user/verify").anonymous()
                .requestMatchers(HttpMethod.POST, "/api/user/verify/reset").anonymous()
                .requestMatchers(HttpMethod.GET, "/api/user/timezone").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/user/credentials/reset/request").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/user/credentials/reset/confirm").permitAll()
                .anyRequest().authenticated())
            .httpBasic(httpBasic -> httpBasic
                .authenticationEntryPoint(new NoPopupBasicAuthenticationEntryPoint(handlerExceptionResolver)))
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
            .sessionManagement(withDefaults())
            .rememberMe(remmeberMe -> remmeberMe.alwaysRemember(true))
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
            .logout(logout -> logout
                .logoutUrl("/api/user/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                .deleteCookies("JSESSIONID", "remember-me")
                .clearAuthentication(true)
                .invalidateHttpSession(true))
            .build();
    }
}
