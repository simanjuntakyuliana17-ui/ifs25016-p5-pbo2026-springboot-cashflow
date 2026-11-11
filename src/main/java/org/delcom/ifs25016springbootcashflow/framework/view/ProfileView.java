package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25016springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25016springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25016springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25016springbootcashflow.usecase.UserUseCase;

/** Pengaturan profil: nama, password (opsional), dan foto. */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil - Cashflow")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {
    private final UserUseCase userUseCase;
    private final FileStorage storage;
    private final IUserRepository users;
    private final User user;
    private final PendingUpload photo = new PendingUpload();

    public ProfileView(UserUseCase userUseCase, FileStorage storage, IUserRepository users) {
        this.userUseCase = userUseCase;
        this.storage = storage;
        this.users = users;
        this.user = AuthSession.currentUser(users);
        if (user == null) {
            return;
        }
        setPadding(true);
        Avatar avatar = new Avatar(user.getFullName());
        avatar.setHeight("5rem");
        avatar.setWidth("5rem");
        if (user.hasPhoto()) {
            avatar.setImage("/uploads/" + user.getPhotoPath());
        }
        TextField username = new TextField("Username");
        username.setValue(user.getUsername());
        username.setReadOnly(true);
        TextField fullName = new TextField("Nama lengkap");
        fullName.setValue(user.getFullName());
        PasswordField password = new PasswordField("Password baru (kosongkan bila tidak diubah)");
        username.setWidthFull();
        fullName.setWidthFull();
        password.setWidthFull();
        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);
        Button save = new Button("Simpan Perubahan", VaadinIcon.CHECK.create());
        save.addClassName("btn-accent");
        save.addClickListener(e -> {
            progress.setVisible(true);
            save.setEnabled(false);
            if (save(fullName.getValue(), password.getValue())) {
                UI.getCurrent().getPage().reload(); // menyegarkan avatar di sidebar
            }
            progress.setVisible(false);
            save.setEnabled(true);
        });
        PageCard card = new PageCard(avatar, username, fullName, password,
                new Span("Foto profil (maks. 2 MB)"), photo.component("Pilih foto"), progress, save);
        card.addClassName("form-card");
        add(new H2("Profil Saya"), card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) == null) {
            event.forwardTo(LoginView.class);
        }
    }

    private boolean save(String fullName, String newPassword) {
        String newPhoto = null;
        try {
            userUseCase.updateProfile(user.getId(), fullName, newPassword);
            if (photo.isPresent()) {
                newPhoto = storage.save(photo.name(), photo.stream());
                String old = user.getPhotoPath();
                userUseCase.updatePhoto(user.getId(), newPhoto);
                storage.delete(old);
            }
            Notices.success("Profil berhasil diperbarui.");
            return true;
        } catch (DomainException ex) {
            storage.delete(newPhoto);
            Notices.error("[!] " + ex.getMessage());
            return false;
        }
    }
}
