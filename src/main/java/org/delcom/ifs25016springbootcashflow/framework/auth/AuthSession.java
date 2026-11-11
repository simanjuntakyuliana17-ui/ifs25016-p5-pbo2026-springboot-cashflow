package org.delcom.ifs25016springbootcashflow.framework.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.repository.IUserRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Sesi login: user disimpan di VaadinSession dan cookie bertanda tangan (HMAC) agar sesi pulih
 * setelah refresh. LOGGED_OUT_KEY mencegah pemulihan otomatis setelah logout.
 */
public final class AuthSession {
    private static final String USER_ID_KEY = "cashflow.userId";
    private static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    private static final String COOKIE_NAME = "cashflow_uid";
    private static final int COOKIE_AGE = 8 * 60 * 60;
    private static volatile String secret = "cashflow-dev-secret";

    private AuthSession() {
    }

    public static void configure(String sessionSecret) {
        secret = sessionSecret;
    }

    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_ID_KEY, user.getId());
        session.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
        writeCookie(user.getId() + "." + sign(String.valueOf(user.getId())), COOKIE_AGE);
    }

    /** Pengguna yang sedang login, atau null bila belum login. */
    public static User currentUser(IUserRepository users) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return null;
        }
        Integer id = (Integer) session.getAttribute(USER_ID_KEY);
        if (id == null && !Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))) {
            id = idFromCookie();
            if (id != null) {
                session.setAttribute(USER_ID_KEY, id);
            }
        }
        return id == null ? null : users.findById(id).orElse(null);
    }

    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        session.setAttribute(USER_ID_KEY, null);
        writeCookie("", 0);
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setLocation("login");
        }
        session.close();
    }

    private static Integer idFromCookie() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        Cookie[] cookies = request == null ? null : request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                String[] parts = cookie.getValue().split("\\.", 2);
                if (parts.length == 2 && parts[0].matches("\\d{1,9}") && MessageDigest.isEqual(
                        sign(parts[0]).getBytes(StandardCharsets.UTF_8), parts[1].getBytes(StandardCharsets.UTF_8))) {
                    return Integer.valueOf(parts[0]);
                }
            }
        }
        return null;
    }

    private static void writeCookie(String value, int maxAge) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response != null) {
            Cookie cookie = new Cookie(COOKIE_NAME, value);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(maxAge);
            response.addCookie(cookie);
        }
    }

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("HMAC tidak tersedia", ex);
        }
    }
}
