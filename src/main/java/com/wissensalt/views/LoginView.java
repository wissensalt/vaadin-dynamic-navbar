package com.wissensalt.views;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.wissensalt.service.SecurityService;
import com.wissensalt.views.helloworld.HelloWorldView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.context.SecurityContextRepository;

@Route("login")
@PageTitle("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout
    implements BeforeEnterObserver {

  private final LoginForm loginForm;
  private final transient UserDetailsManager userDetailsManager;
  private final transient AuthenticationManager authenticationManager;
  private final transient RedisTemplate<String, Object> redisTemplate;
  private final transient SecurityService securityService;
  private final transient SecurityContextRepository securityContextRepository;

  public LoginView(
      UserDetailsManager userDetailsManager,
      AuthenticationManager authenticationManager,
      RedisTemplate<String, Object> redisTemplate,
      SecurityService securityService,
      SecurityContextRepository securityContextRepository
  ) {
    this.securityContextRepository = securityContextRepository;
    this.userDetailsManager = userDetailsManager;
    this.authenticationManager = authenticationManager;
    this.redisTemplate = redisTemplate;
    this.securityService = securityService;
    this.addClassName(Background.CONTRAST);
    this.setSizeFull();
    setJustifyContentMode(JustifyContentMode.CENTER);
    setAlignItems(Alignment.CENTER);
    LoginI18n i18n = LoginI18n.createDefault();
    LoginI18n.Form i18nForm = i18n.getForm();
    i18nForm.setTitle("Login App with Vaadin");
    i18nForm.setUsername("Username");
    i18nForm.setPassword("Password");
    i18nForm.setSubmit("Submit");
    i18n.setForm(i18nForm);

    LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
    i18nErrorMessage.setTitle("Wrong username/ password");
    i18nErrorMessage.setMessage("Please check your username/ Password.");
    i18n.setErrorMessage(i18nErrorMessage);

    loginForm = new LoginForm();
    loginForm.setI18n(i18n);
    loginForm.addLoginListener(loginEventComponentEventListener());
    add(loginForm);
  }

  private ComponentEventListener<AbstractLogin.LoginEvent> loginEventComponentEventListener() {

    return loginEvent -> {
      final String username = loginEvent.getUsername();
      final String password = loginEvent.getPassword();
      UserDetails userDetails;
      try {
        userDetails = userDetailsManager.loadUserByUsername(username);
      } catch (UsernameNotFoundException usernameNotFoundException) {
        Notification.show("User Not Found");
        loginForm.setError(true);

        return;
      }

      Authentication authentication = null;
      try {
        authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password));
      } catch (AuthenticationException authenticationException) {
        loginForm.setError(true);
      }

      if (authentication != null) {
        final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        final String sessionId = VaadinSession.getCurrent().getSession().getId();
        redisTemplate.opsForValue().set(sessionId, userDetails, Duration.ofMinutes(30));
        securityContextRepository.saveContext(securityContext,
            (HttpServletRequest) VaadinRequest.getCurrent(),
            (HttpServletResponse) VaadinResponse.getCurrent());
        loginForm.getUI().ifPresent(ui -> ui.navigate(""));
      }
    };
  }

  @Override
  public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
    if (beforeEnterEvent.getLocation().getQueryParameters().getParameters().containsKey("error")) {
      loginForm.setError(true);
    }

    final Authentication authentication = securityService.getUser();

    if (beforeEnterEvent.getNavigationTarget().equals(LoginView.class)) {
      if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
        beforeEnterEvent.forwardTo(HelloWorldView.class);
      }
    } else {
      beforeEnterEvent.forwardTo(LoginView.class);
    }
  }
}
