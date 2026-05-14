package com.ejemplo.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.ejemplo.model.User;
import com.ejemplo.model.UserDAO;
import com.ejemplo.service.SchemaInitializer;
import com.ejemplo.util.ErrorUtil;
import com.ejemplo.util.JsonRequestUtil;
import com.ejemplo.util.ServletResponseUtil;
import com.ejemplo.util.SessionUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/auth/register")
public class RegisterServlet extends HttpServlet {

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
            String username = JsonRequestUtil.obtenerTexto(datos, "username").toLowerCase();
            String email = JsonRequestUtil.obtenerTexto(datos, "email").toLowerCase();
            String password = JsonRequestUtil.obtenerTexto(datos, "password");

            validarRegistro(username, email, password);

            if (userDAO.existeUsername(username)) {
                throw new IllegalArgumentException("Ese nombre de usuario ya existe");
            }

            if (userDAO.existeEmail(email)) {
                throw new IllegalArgumentException("Ese email ya esta registrado");
            }

            User user = userDAO.registrar(username, email, password);
            SessionUtil.iniciarSesion(request, user);
            ServletResponseUtil.writeJson(response, gson, Map.of("ok", true, "user", SessionUtil.getUserData(request)));
        } catch (IllegalArgumentException e) {
            ServletResponseUtil.writeError(response, gson, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            Map<String, Object> error = ServletResponseUtil.crearError("No se pudo crear la cuenta");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            ServletResponseUtil.writeJson(response, gson, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    }

    private void validarRegistro(String username, String email, String password) {
        if (username.length() < 3 || username.length() > 30) {
            throw new IllegalArgumentException("El usuario debe tener entre 3 y 30 caracteres");
        }

        if (!username.matches("[a-z0-9._-]+")) {
            throw new IllegalArgumentException("El usuario solo puede llevar letras minusculas, numeros, punto, guion y guion bajo");
        }

        if (email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Introduce un email valido");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
    }
}
