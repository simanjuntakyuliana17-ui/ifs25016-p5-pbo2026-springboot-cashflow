package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
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

/** Halaman registrasi (publik). Setelah berhasil, pengguna langsung masuk. */
@Route("register")
@PageTitle("Daftar - Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthUseCase authUseCase;
    private final IUserRepository users;
    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password (min. 6 karakter)");
    private final PasswordField confirm = new PasswordField("Ulangi password");
    private final Button submit = new Button("Daftar");
    private final ProgressBar progress = new ProgressBar();

    public RegisterView(AuthUseCase authUseCase, IUserRepository users) {
        this.authUseCase = authUseCase;
        this.users = users;
        addClassName("auth-page");
        getElement().setAttribute("role", "main");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        InputIds.onInput(fullName, "register-fullname");
        InputIds.onInput(username, "register-username");
        InputIds.onInput(password, "register-password");
        InputIds.onInput(confirm, "register-confirm");
        submit.setId("register-submit");
        fullName.setWidthFull();
        username.setWidthFull();
        password.setWidthFull();
        confirm.setWidthFull();
        submit.setWidthFull();
        submit.addClassName("btn-accent");
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickShortcut(Key.ENTER);
        submit.addClickListener(e -> register());
        progress.setIndeterminate(true);
        progress.setVisible(false);

        PageCard card = new PageCard(new H1("Buat Akun"), new Paragraph("Daftar gratis untuk mulai mencatat."),
                fullName, username, password, confirm, progress, submit,
                new Paragraph(new Span("Sudah punya akun? "), new RouterLink("Masuk", LoginView.class)));
        card.addClassName("auth-card");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) != null) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void register() {
        progress.setVisible(true);
        submit.setEnabled(false);
        try {
            User user = authUseCase.register(fullName.getValue(), username.getValue(),
                    password.getValue(), confirm.getValue());
            AuthSession.login(user);
            Notices.success("Registrasi berhasil. Selamat datang, " + user.getFullName() + "!");
            UI.getCurrent().navigate(FinanceView.class);
        } catch (ValidationException ex) {
            Notices.error(ex.getMessage());
        } finally {
            progress.setVisible(false);
            submit.setEnabled(true);
        }
    }
}
