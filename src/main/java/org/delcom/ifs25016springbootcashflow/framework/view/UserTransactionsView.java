package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25016springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25016springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25016springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25016springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25016springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25016springbootcashflow.usecase.UserUseCase;

import java.util.List;

/** Transaksi pengguna lain, read-only (ADMIN saja). ID pengguna dibaca dari ?userId=. */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna - Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {
    private final FinanceUseCase financeUseCase;
    private final UserUseCase userUseCase;
    private final FinancePresenter presenter;
    private final IUserRepository users;

    public UserTransactionsView(FinanceUseCase financeUseCase, UserUseCase userUseCase,
                                FinancePresenter presenter, IUserRepository users) {
        this.financeUseCase = financeUseCase;
        this.userUseCase = userUseCase;
        this.presenter = presenter;
        this.users = users;
        setPadding(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User actor = AuthSession.currentUser(users);
        if (actor == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!actor.isAdmin()) {
            event.forwardTo(FinanceView.class);
            return;
        }
        List<String> param = event.getLocation().getQueryParameters().getParameters().getOrDefault("userId", List.of());
        try {
            int targetId = Integer.parseInt(param.isEmpty() ? "" : param.get(0));
            build(actor, userUseCase.getUser(targetId));
        } catch (NumberFormatException | DomainException ex) {
            Notices.error(presenter.showError("Pengguna tidak ditemukan"));
            event.forwardTo(UsersView.class);
        }
    }

    private void build(User actor, User target) throws DomainException {
        removeAll();
        List<Transaction> items = financeUseCase.viewAsAdmin(actor, target.getId());
        Grid<Transaction> grid = TransactionGrid.create(presenter, "Transaksi " + target.getFullName());
        grid.setItems(items);
        Button back = new Button("Kembali", VaadinIcon.ARROW_LEFT.create(), e -> UI.getCurrent().navigate(UsersView.class));
        back.addClassName("btn-secondary");
        add(back, new H2("Transaksi " + target.getFullName() + " (read-only)"),
                new Paragraph("Pemasukan " + presenter.formatMoney(financeUseCase.getTotalIncome(target.getId()))
                        + " · Pengeluaran " + presenter.formatMoney(financeUseCase.getTotalExpense(target.getId()))
                        + " · " + presenter.formatBalance(financeUseCase.getBalance(target.getId()))),
                grid);
    }
}
