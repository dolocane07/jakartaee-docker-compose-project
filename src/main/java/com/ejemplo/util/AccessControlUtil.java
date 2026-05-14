package com.ejemplo.util;

import java.io.IOException;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class AccessControlUtil {

    private AccessControlUtil() {
    }

    public static Integer requireLoggedUser(HttpServletRequest request, HttpServletResponse response, Gson gson,
                                            String unauthorizedMessage) throws IOException {
        Integer userId = SessionUtil.getUserId(request);
        if (userId == null) {
            ServletResponseUtil.writeError(response, gson, HttpServletResponse.SC_UNAUTHORIZED, unauthorizedMessage);
            return null;
        }

        return userId;
    }

    public static boolean requireAdmin(HttpServletRequest request, HttpServletResponse response, Gson gson,
                                       String forbiddenMessage) throws IOException {
        if (!SessionUtil.isAdmin(request)) {
            ServletResponseUtil.writeError(response, gson, HttpServletResponse.SC_FORBIDDEN, forbiddenMessage);
            return false;
        }

        return true;
    }

    public static boolean requireStandardUser(HttpServletRequest request, HttpServletResponse response, Gson gson,
                                              String forbiddenMessage) throws IOException {
        if (SessionUtil.isAdmin(request)) {
            ServletResponseUtil.writeError(response, gson, HttpServletResponse.SC_FORBIDDEN, forbiddenMessage);
            return false;
        }

        return true;
    }
}
