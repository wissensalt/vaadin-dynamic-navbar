package com.wissensalt.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class SessionFilter extends OncePerRequestFilter {

  private final RedisTemplate<String, Object> redisTemplate;
  private final SecurityContextRepository securityContextRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    final HttpSession session = request.getSession();
    final String sessionId = session.getId();
    final UserDetails userDetails = (UserDetails) redisTemplate.opsForValue().get(sessionId);
    if (userDetails == null) {
      SecurityContextHolder.clearContext();
      final SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
      securityContextRepository.saveContext(emptyContext, request, response);
    } else {
      final Authentication authentication = new UsernamePasswordAuthenticationToken(
          userDetails, userDetails.getPassword(), userDetails.getAuthorities());

      SecurityContext securityContext = new SecurityContextImpl();
      securityContext.setAuthentication(authentication);
      SecurityContextHolder.setContext(securityContext);
    }

    filterChain.doFilter(request, response);
  }
}
