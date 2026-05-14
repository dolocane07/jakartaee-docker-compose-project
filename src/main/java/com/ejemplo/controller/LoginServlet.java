package com.ejemplo.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.ejemplo.model.User;
import com.ejemplo.model.UserDAO;
import com.ejemplo.service.SchemaInitializer;
import com.ejemplo.util.ErrorUtil;
import com.ejemplo.util.JsonRequestUtil;
import com.ejemplo.util.PasswordUtil;
import com.ejemplo.util.ServletResponseUtil;
import com.ejemplo.util.SessionUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/auth/login")
public class LoginServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final UserDAO userDAO = new UserDAO();
    private final SchemaInitializer schemaInitializer = new SchemaInitializer();

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        try {
            schemaInitializer.ensureSchema();

            Map<String, Object> datos = JsonRequestUtil.leerJson(request, gson);
            String identifier = JsonRequestUtil.obtenerTexto(datos, "identifier").toLowerCase();
            String password = JsonRequestUtil.obtenerTexto(datos, "password");

            if (identifier.isBlank() || password.isBlank()) {
                throw new IllegalArgumentException("Completa usuario/email y contraseña");
            }

            User user = userDAO.buscarPorIdentifier(identifier);
            if (user == null || !PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                user = null;
            }

            if (user == null) {
                ServletResponseUtil.writeError(response, gson, HttpServletResponse.SC_UNAUTHORIZED,
                        "Credenciales incorrectas");
                return;
            }

            SessionUtil.iniciarSesion(request, user);
            ServletResponseUtil.writeJson(response, gson, Map.of("ok", true, "user", SessionUtil.getUserData(request)));
        } catch (IllegalArgumentException e) {
            ServletResponseUtil.writeError(response, gson, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            Map<String, Object> error = ServletResponseUtil.crearError("No se pudo iniciar sesion");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            ServletResponseUtil.writeJson(response, gson, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    }
}
