package secretSanta.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import secretSanta.security.Roles;
import secretSanta.security.AuthenticationManagerCustom;

import javax.sql.DataSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private DataSource dataSource;
    private AuthenticationManagerCustom authenticationManagerCustom;

    private static final String[] AUTH_WHITELIST = {
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/webjars/**",
            "/login", "/register"
    };

    public SecurityConfig(DataSource dataSource, AuthenticationManagerCustom authenticationManagerCustom) {
        this.dataSource = dataSource;
        this.authenticationManagerCustom = authenticationManagerCustom;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests(authz ->
                        authz
                                .requestMatchers(AUTH_WHITELIST).permitAll()
                                .anyRequest().hasAuthority(Roles.CLIENT.getRole())
                )
                .cors()
                .and().httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager() {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);
        jdbcUserDetailsManager.setAuthenticationManager(authenticationManagerCustom);
        return jdbcUserDetailsManager;
    }

}
