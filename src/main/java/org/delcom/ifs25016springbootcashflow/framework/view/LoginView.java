package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25016springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25016springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25016springbootcashflow.usecase.AuthUseCase;

/** Halaman login (publik). Pengguna yang sudah login dialihkan ke halaman utama. */
@Route("login")
@PageTitle("Masuk - Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthUseCase authUseCase;
    private final IUserRepository users;
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final Button submit = new Button("Masuk");
    private final ProgressBar progress = new ProgressBar();

    public LoginView(AuthUseCase authUseCase, IUserRepository users) {
        this.authUseCase = authUseCase;
        this.users = users;
        addClassName("auth-page");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        username.setId("login-username");
        username.setWidthFull();
        password.setId("login-password");
        password.setWidthFull();
        submit.setId("login-submit");
        submit.setWidthFull();
        submit.addClassName("btn-accent");
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickShortcut(Key.ENTER);
        submit.addClickListener(e -> login());
        progress.setIndeterminate(true);
        progress.setVisible(false);

        RouterLink register = new RouterLink("Daftar sekarang", RegisterView.class);
        PageCard card = new PageCard(new H1("Cashflow"), new Paragraph("Masuk untuk mencatat keuangan Anda."),
                username, password, progress, submit,
                new Paragraph(new com.vaadin.flow.component.html.Span("Belum punya akun? "), register));
        card.addClassName("auth-card");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) != null) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void login() {
        progress.setVisible(true);
        submit.setEnabled(false);
        try {
            User user = authUseCase.login(username.getValue(), password.getValue());
            AuthSession.login(user);
            UI.getCurrent().navigate(FinanceView.class);
        } catch (ValidationException ex) {
            Notices.error(ex.getMessage());
        } finally {
            progress.setVisible(false);
            submit.setEnabled(true);
        }
    }
}
