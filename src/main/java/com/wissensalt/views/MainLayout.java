package com.wissensalt.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wissensalt.service.SecurityService;
import com.wissensalt.views.about.AboutView;
import com.wissensalt.views.helloworld.HelloWorldView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

  private H2 viewTitle;
  private final SecurityService securityService;

  public MainLayout(SecurityService securityService) {
    this.securityService = securityService;
    setPrimarySection(Section.DRAWER);
    addDrawerContent();
    addHeaderContent();
  }

  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.setAriaLabel("Menu toggle");

    viewTitle = new H2();
    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
    final Button logoutButton = new Button("Logout");

    logoutButton.addClickListener(e -> securityService.logout());
    final HorizontalLayout header = new HorizontalLayout(toggle, viewTitle, logoutButton,
        logoutButton);
    header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    header.expand(viewTitle);
    header.setWidthFull();

    addToNavbar(true, header);
  }

  private void addDrawerContent() {
    H1 appName = new H1("Vaadin Dynamic NavBar");
    appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
    Header header = new Header(appName);

    Scroller scroller = new Scroller(createNavigation());

    addToDrawer(header, scroller, createFooter());
  }

  private SideNav createNavigation() {
    final Authentication authentication = securityService.getUser();
    SideNav nav = new SideNav();
    final User user = (User) authentication.getPrincipal();
    nav.addItem(
        new SideNavItem("Hello World", HelloWorldView.class, LineAwesomeIcon.GLOBE_SOLID.create()));
    if (user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
      nav.addItem(new SideNavItem("About", AboutView.class, LineAwesomeIcon.FILE.create()));
    }

    return nav;
  }

  private Footer createFooter() {
    Footer layout = new Footer();

    return layout;
  }

  @Override
  protected void afterNavigation() {
    super.afterNavigation();
    viewTitle.setText(getCurrentPageTitle());
  }

  private String getCurrentPageTitle() {
    PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
    return title == null ? "" : title.value();
  }
}
