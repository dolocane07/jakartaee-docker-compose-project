package com.ejemplo.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletResponse;

public final class ServletResponseUtil {

    private ServletResponseUtil() {
    }

    public static void writeJson(HttpServletResponse response, Gson gson, Map<String, Object> body) throws IOException {
        response.getWriter().write(gson.toJson(body));
    }

    public static void writeJson(HttpServletResponse response, Gson gson, int status, Map<String, Object> body)
            throws IOException {
        response.setStatus(status);
        writeJson(response, gson, body);
    }

    public static void writeError(HttpServletResponse response, Gson gson, int status, String message) throws IOException {
        response.setStatus(status);
        writeJson(response, gson, crearError(message));
    }

    public static Map<String, Object> crearError(String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("ok", false);
        error.put("mensaje", mensaje);
        return error;
    }
}
