package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25016springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25016springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25016springbootcashflow.usecase.UserUseCase;

import java.util.Map;

/** Daftar seluruh pengguna (ADMIN saja). */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna - Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {
    private final UserUseCase userUseCase;
    private final IUserRepository users;

    public UsersView(UserUseCase userUseCase, IUserRepository users) {
        this.userUseCase = userUseCase;
        this.users = users;
        setPadding(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User actor = AuthSession.currentUser(users);
        if (actor == null) {
            event.forwardTo(LoginView.class);
        } else if (!actor.isAdmin()) {
            event.forwardTo(FinanceView.class);
        } else {
            build(actor);
        }
    }

    private void build(User actor) {
        removeAll();
        Grid<User> grid = new Grid<>();
        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0).setSortable(true);
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(User::getFullName).setHeader("Nama lengkap").setFlexGrow(1).setSortable(true);
        grid.addColumn(u -> u.getRole().label()).setHeader("Peran").setAutoWidth(true).setSortable(true);
        grid.addComponentColumn(u -> {
            Button open = new Button("Lihat Transaksi", VaadinIcon.EYE.create(), e -> UI.getCurrent().navigate(
                    "user-transactions", QueryParameters.simple(Map.of("userId", String.valueOf(u.getId())))));
            open.addClassName("btn-text");
            open.getElement().setAttribute("aria-label", "Lihat transaksi " + u.getUsername());
            return open;
        }).setHeader("Tindakan").setAutoWidth(true).setFlexGrow(0);
        GridStyles.apply(grid, "Daftar pengguna");
        try {
            grid.setItems(userUseCase.listUsers(actor));
        } catch (ValidationException ex) {
            Notices.error(ex.getMessage());
        }
        add(new H2("Daftar Pengguna"), grid);
    }
}
