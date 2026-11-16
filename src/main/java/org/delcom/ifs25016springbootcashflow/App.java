package org.delcom.ifs25016springbootcashflow;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
@PWA(name = "Cashflow", shortName = "Cashflow", iconPath = "icons/icon.png")
@Meta(name = "description",
        content = "Cashflow: aplikasi web pencatat pemasukan, pengeluaran, dan saldo keuangan pribadi.")
public class App implements AppShellConfigurator {

    /** Judul bawaan yang sudah ada di HTML awal (sebelum JavaScript berjalan), untuk SEO dan aksesibilitas. */
    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setPageTitle("Cashflow - Catatan Keuangan Pribadi");
    }

    public static void main(String[] args) throws Exception {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}
