package com.ejemplo.util;

import java.util.HashMap;
import java.util.Map;

import com.ejemplo.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionUtil {

    private static final String USER_ID = "userId";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email";

    private SessionUtil() {
    }

    public static void iniciarSesion(HttpServletRequest request, User user) {
        HttpSession actual = request.getSession(false);
        if (actual != null) {
            actual.invalidate();
        }

        HttpSession nueva = request.getSession(true);
        nueva.setAttribute(USER_ID, user.getId());
        nueva.setAttribute(USERNAME, user.getUsername());
        nueva.setAttribute(EMAIL, user.getEmail());
        nueva.setMaxInactiveInterval(60 * 60 * 24 * 14);
    }

    public static void cerrarSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static Integer getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object valor = session.getAttribute(USER_ID);
        return valor instanceof Integer entero ? entero : null;
    }

    public static Map<String, Object> getUserData(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Integer userId = getUserId(request);
        if (userId == null) {
            return null;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("username", session.getAttribute(USERNAME));
        user.put("email", session.getAttribute(EMAIL));
        return user;
    }
}
