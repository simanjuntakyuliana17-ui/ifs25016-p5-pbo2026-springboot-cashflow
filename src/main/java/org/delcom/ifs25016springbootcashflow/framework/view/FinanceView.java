package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25016springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25016springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25016springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25016springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25016springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25016springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25016springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25016springbootcashflow.usecase.FinanceUseCase;

import java.util.List;

/** Dashboard transaksi milik pengguna yang login (USER dan ADMIN). */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Transaksi - Cashflow")
public class FinanceView extends VerticalLayout implements BeforeEnterObserver {
    private final FinanceUseCase useCase;
    private final FinancePresenter presenter;
    private final FileStorage storage;
    private final IUserRepository users;
    private final User user;

    private final Grid<Transaction> grid;
    private final TextField search = new TextField("Cari keterangan");
    private final Select<SortOption> sort = new Select<>();
    private final Paragraph status = new Paragraph();
    private final Span emptyState = new Span();
    private final Span total = new Span();
    private final Span income = new Span();
    private final Span expense = new Span();
    private final Span balance = new Span();

    public FinanceView(FinanceUseCase useCase, FinancePresenter presenter, FileStorage storage,
                       IUserRepository users) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.storage = storage;
        this.users = users;
        this.user = AuthSession.currentUser(users);
        this.grid = TransactionGrid.create(presenter, "Daftar transaksi keuangan");
        if (user == null) {
            return; // BeforeEnter akan mengalihkan ke /login
        }
        setPadding(true);
        getElement().setAttribute("role", "main");
        grid.addComponentColumn(this::actions).setHeader("Tindakan").setAutoWidth(true).setFlexGrow(0);
        grid.setEmptyStateComponent(emptyState);

        add(new H2("Catatan Keuangan"), summary(), toolbar(), status, grid);
        showAll();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) == null) {
            event.forwardTo(LoginView.class);
        }
    }

    // ---------- tampilan ----------
    private Div summary() {
        Div row = new Div(card("Total Transaksi", total, "blue"), card("Total Pemasukan", income, "green"),
                card("Total Pengeluaran", expense, "red"), card("Saldo", balance, "amber"));
        row.addClassName("summary-grid");
        return row;
    }

    private Div card(String label, Span value, String color) {
        Span caption = new Span(label);
        caption.addClassName("summary-label");
        value.addClassName("summary-value");
        Div card = new Div(caption, value);
        card.addClassNames("summary-card", color);
        return card;
    }

    private HorizontalLayout toolbar() {
        search.setPlaceholder("Ketik kata kunci...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setMinWidth("12rem");
        search.addKeyDownListener(com.vaadin.flow.component.Key.ENTER, e -> doSearch());
        sort.setLabel("Urutkan");
        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::label);
        sort.setPlaceholder("Pilih urutan");
        sort.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                show(useCase.sort(user.getId(), e.getValue()), "Diurutkan: " + e.getValue().label());
            }
        });
        Button find = styled(new Button("Cari", VaadinIcon.SEARCH.create(), e -> doSearch()), "btn-secondary");
        Button reload = styled(new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> showAll()), "btn-secondary");
        Button addIncome = styled(new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                e -> openForm(TransactionType.INCOME, null)), "btn-income");
        Button addExpense = styled(new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                e -> openForm(TransactionType.EXPENSE, null)), "btn-expense");
        HorizontalLayout bar = new HorizontalLayout(search, find, sort, reload, addIncome, addExpense);
        bar.setWidthFull();
        bar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        bar.setFlexGrow(1, search);
        bar.getStyle().set("flex-wrap", "wrap");
        return bar;
    }

    private Button styled(Button button, String cssClass) {
        button.addClassName(cssClass);
        return button;
    }

    private HorizontalLayout actions(Transaction t) {
        Button edit = styled(new Button("Ubah", VaadinIcon.EDIT.create(), e -> openForm(t.getType(), t)), "btn-text");
        edit.getElement().setAttribute("aria-label", "Ubah transaksi " + t.getId());
        Button delete = styled(new Button("Hapus", VaadinIcon.TRASH.create(), e -> openDelete(t)), "btn-danger-text");
        delete.getElement().setAttribute("aria-label", "Hapus transaksi " + t.getId());
        return new HorizontalLayout(edit, delete);
    }

    // ---------- data ----------
    private void showAll() {
        emptyState.setText(presenter.showEmpty());
        search.clear();
        sort.clear();
        show(useCase.getTransactions(user.getId()), "Daftar Transaksi");
    }

    private void doSearch() {
        if (search.getValue().isBlank()) {
            showAll();
            return;
        }
        emptyState.setText(presenter.showNotFound());
        show(useCase.search(user.getId(), search.getValue()), "Hasil pencarian: \"" + search.getValue() + "\"");
    }

    private void show(List<Transaction> items, String title) {
        grid.setItems(items);
        total.setText(String.valueOf(useCase.getTransactions(user.getId()).size()));
        income.setText(presenter.formatMoney(useCase.getTotalIncome(user.getId())));
        expense.setText(presenter.formatMoney(useCase.getTotalExpense(user.getId())));
        balance.setText(presenter.formatMoney(useCase.getBalance(user.getId())));
        status.setText(title + " (" + items.size() + ")");
    }

    // ---------- dialog ----------
    private void openForm(TransactionType type, Transaction editing) {
        TextField description = new TextField("Keterangan");
        TextField amount = new TextField("Jumlah (Rp)");
        description.setWidthFull();
        amount.setWidthFull();
        amount.setAllowedCharPattern("[0-9]");
        amount.getElement().setAttribute("inputmode", "numeric");
        PendingUpload pending = new PendingUpload();
        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        VerticalLayout content = new VerticalLayout(description, amount);
        if (editing == null) {
            content.add(new Span("Bukti transaksi (opsional, maks. 2 MB)"), pending.component("Pilih gambar bukti"));
        } else {
            description.setValue(editing.getDescription());
            amount.setValue(String.valueOf(editing.getAmount()));
        }
        content.add(progress);

        Dialog dialog = new Dialog(content);
        DialogStyles.apply(dialog, (editing == null ? "Tambah " : "Ubah ") + type.label(), type.label());
        dialog.setWidth("min(460px, 95vw)");
        Button save = styled(new Button("Simpan", VaadinIcon.CHECK.create()), "btn-accent");
        save.addClickListener(e -> {
            progress.setVisible(true);
            save.setEnabled(false);
            if (saveForm(type, editing, description.getValue(), amount.getValue(), pending)) {
                dialog.close();
                showAll();
            }
            progress.setVisible(false);
            save.setEnabled(true);
        });
        dialog.getFooter().add(new Button("Batal", e -> dialog.close()), save);
        dialog.open();
    }

    private boolean saveForm(TransactionType type, Transaction editing, String description, String amount,
                             PendingUpload pending) {
        String proof = null;
        try {
            if (editing != null) {
                Transaction updated = useCase.update(user.getId(), editing.getId(), description, amount);
                Notices.success(presenter.showUpdateSuccess(updated));
                return true;
            }
            if (pending.isPresent()) {
                proof = storage.save(pending.name(), pending.stream());
            }
            Transaction created = type == TransactionType.INCOME
                    ? useCase.addIncome(user.getId(), description, amount, proof)
                    : useCase.addExpense(user.getId(), description, amount, proof);
            Notices.success(presenter.showAddSuccess(created));
            return true;
        } catch (DomainException ex) {
            storage.delete(proof); // bersihkan file bila validasi gagal
            Notices.error(presenter.showError(ex.getMessage()));
            return false;
        }
    }

    private void openDelete(Transaction t) {
        Dialog dialog = new Dialog(new Paragraph("Yakin ingin menghapus transaksi \"" + t.getDescription()
                + "\" (" + presenter.formatMoney(t.getAmount()) + ")? Tindakan ini tidak dapat dibatalkan."));
        DialogStyles.apply(dialog, "Hapus Transaksi", "Konfirmasi");
        Button confirm = styled(new Button("Hapus", VaadinIcon.TRASH.create(), e -> {
            try {
                Transaction removed = useCase.remove(user.getId(), t.getId());
                storage.delete(removed.getProofPath());
                Notices.success(presenter.showRemoveSuccess());
            } catch (DomainException ex) {
                Notices.error(presenter.showError(ex.getMessage()));
            }
            dialog.close();
            showAll();
        }), "btn-expense");
        dialog.getFooter().add(new Button("Batal", e -> dialog.close()), confirm);
        dialog.open();
    }
}
