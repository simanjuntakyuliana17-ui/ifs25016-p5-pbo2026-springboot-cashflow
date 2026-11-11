package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25016springbootcashflow.framework.auth.AuthSession;

/** Layout utama: sidebar navigasi, menu avatar (Profil / Keluar), dan penjaga login global. */
public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private final IUserRepository users;
    private int builtForUserId = -1;

    public MainLayout(IUserRepository users) {
        this.users = users;
        setPrimarySection(Section.DRAWER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(users);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (builtForUserId != user.getId()) {
            build(user);
            builtForUserId = user.getId();
        }
    }

    private void build(User user) {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Buka atau tutup menu");
        H1 title = new H1("Cashflow");
        title.addClassName("app-title");

        Avatar avatar = new Avatar(user.getFullName());
        if (user.hasPhoto()) {
            avatar.setImage("/uploads/" + user.getPhotoPath());
        }
        MenuBar menu = new MenuBar();
        menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        menu.getElement().setAttribute("aria-label", "Menu akun");
        var root = menu.addItem(avatar);
        root.getSubMenu().addItem("Profil", e -> UI.getCurrent().navigate(ProfileView.class));
        root.getSubMenu().addItem("Keluar", e -> AuthSession.logout());

        Div spacer = new Div();
        spacer.addClassName("grow");
        Span who = new Span(user.getFullName() + " · " + user.getRole().label());
        who.addClassName("who");
        addToNavbar(toggle, title, spacer, who, menu);

        VerticalLayout nav = new VerticalLayout();
        nav.addClassName("nav");
        nav.add(link("Transaksi", VaadinIcon.WALLET, FinanceView.class),
                link("Profil", VaadinIcon.USER, ProfileView.class));
        if (user.isAdmin()) {
            nav.add(link("Pengguna", VaadinIcon.USERS, UsersView.class));
        }
        addToDrawer(nav);
    }

    private RouterLink link(String text, VaadinIcon icon, Class<? extends com.vaadin.flow.component.Component> target) {
        RouterLink link = new RouterLink();
        link.setRoute(target);
        link.addClassName("nav-link");
        link.add(icon.create(), new Span(text));
        return link;
    }
}
