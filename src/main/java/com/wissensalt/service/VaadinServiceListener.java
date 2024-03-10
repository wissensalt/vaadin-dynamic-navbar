package com.wissensalt.service;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import com.wissensalt.views.LoginView;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VaadinServiceListener implements VaadinServiceInitListener {

  private final NavigationAccessControl accessControl;

  public VaadinServiceListener() {
    accessControl = new NavigationAccessControl();
    accessControl.setLoginView(LoginView.class);
  }


  @Override
  public void serviceInit(ServiceInitEvent event) {
    event.getSource().addSessionInitListener(
        initEvent -> LoggerFactory.getLogger(getClass())
            .info("A new Session has been initialized!"));

    event.getSource().addSessionDestroyListener(
        initEvent -> LoggerFactory.getLogger(getClass())
            .info("Session has been destroyed!"));

    event.getSource().addUIInitListener(
        initEvent -> {
          LoggerFactory.getLogger(getClass())
              .info("A new UI has been initialized!");
          initEvent.getUI().addBeforeEnterListener(accessControl);
        });
  }
}
