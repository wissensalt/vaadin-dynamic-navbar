package com.wissensalt.config;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import com.wissensalt.views.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .securityContext(httpSecuritySecurityContextConfigurer ->
            httpSecuritySecurityContextConfigurer
                .securityContextRepository(httpSessionSecurityContextRepository()))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(new AntPathRequestMatcher("/public/**")).permitAll())
        .sessionManagement(
            session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true));

    super.configure(http);
    setLoginView(http, LoginView.class);
  }

  @Bean
  public HttpSessionSecurityContextRepository httpSessionSecurityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  public UserDetailsManager userDetailsManager(BCryptPasswordEncoder bCryptPasswordEncoder) {
    final String password = bCryptPasswordEncoder.encode("password");
    final UserDetails userAdmin = User.withUsername("john").password(password)
        .roles("ADMIN").build();
    final UserDetails userRegular = User.withUsername("david").password(password)
        .roles("USER").build();

    return new InMemoryUserDetailsManager(userAdmin, userRegular);
  }
}
