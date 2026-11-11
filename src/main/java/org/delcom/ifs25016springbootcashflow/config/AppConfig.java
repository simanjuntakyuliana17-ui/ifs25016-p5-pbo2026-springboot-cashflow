package org.delcom.ifs25016springbootcashflow.config;

import org.delcom.ifs25016springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25016springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25016springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25016springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25016springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25016springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25016springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25016springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25016springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25016springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25016springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

/** Composition root: seluruh wiring dependency antar-layer ada di sini (bukan new tersebar di view). */
@Configuration
public class AppConfig {

    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    public ITransactionRepository transactionRepository(DataSource dataSource, IUserRepository userRepository) {
        // parameter userRepository memastikan tabel users (FK) sudah dibuat lebih dulu
        return new JdbcTransactionRepository(dataSource);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository userRepository) {
        return new AuthUseCase(userRepository);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository userRepository) {
        return new UserUseCase(userRepository);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactionRepository) {
        return new FinanceUseCase(transactionRepository);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    @Bean
    public FileStorage fileStorage(@Value("${app.upload-dir}") String uploadDir) {
        return new FileStorage(uploadDir);
    }

    @Bean
    public ApplicationRunner seedRootAdmin(AuthUseCase authUseCase,
                                           @Value("${app.root-username}") String rootUsername,
                                           @Value("${app.root-password}") String rootPassword) {
        return args -> authUseCase.ensureRootAdmin(rootUsername, rootPassword);
    }

    @Bean
    public ApplicationRunner configureAuthSession(@Value("${app.session-secret}") String secret) {
        return args -> AuthSession.configure(secret);
    }

    @Bean
    public WebMvcConfigurer uploadResources(FileStorage fileStorage) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**").addResourceLocations(fileStorage.resourceLocation());
            }
        };
    }
}
