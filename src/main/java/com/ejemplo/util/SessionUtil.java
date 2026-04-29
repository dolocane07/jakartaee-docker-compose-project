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
    private static final String IS_ADMIN = "isAdmin";

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
        nueva.setAttribute(IS_ADMIN, user.isAdmin());
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
        user.put("isAdmin", isAdmin(request));
        return user;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Object valor = session.getAttribute(IS_ADMIN);
        return valor instanceof Boolean bool && bool;
    }
}
