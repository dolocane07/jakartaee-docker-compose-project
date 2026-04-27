package com.ejemplo.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.ejemplo.model.User;
import com.ejemplo.model.UserDAO;
import com.ejemplo.service.SchemaInitializer;
import com.ejemplo.util.ErrorUtil;
import com.ejemplo.util.PasswordUtil;
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

            Map<String, Object> datos = leerJson(request);
            String identifier = obtenerTexto(datos, "identifier").toLowerCase();
            String password = obtenerTexto(datos, "password");

            if (identifier.isBlank() || password.isBlank()) {
                throw new IllegalArgumentException("Completa usuario/email y contraseña");
            }

            User user = userDAO.buscarPorIdentifier(identifier);
            if (user == null || !PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(crearError("Credenciales incorrectas")));
                return;
            }

            SessionUtil.iniciarSesion(request, user);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("user", SessionUtil.getUserData(request));
            response.getWriter().write(gson.toJson(respuesta));
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(crearError(e.getMessage())));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = crearError("No se pudo iniciar sesion");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            response.getWriter().write(gson.toJson(error));
        }
    }

    private Map<String, Object> leerJson(HttpServletRequest request) throws IOException {
        StringBuilder json = new StringBuilder();
        BufferedReader reader = request.getReader();
        String linea;

        while ((linea = reader.readLine()) != null) {
            json.append(linea);
        }

        return gson.fromJson(json.toString(), Map.class);
    }

    private String obtenerTexto(Map<String, Object> datos, String clave) {
        if (datos == null || datos.get(clave) == null) {
            return "";
        }
        return String.valueOf(datos.get(clave)).trim();
    }

    private Map<String, Object> crearError(String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("ok", false);
        error.put("mensaje", mensaje);
        return error;
    }
}
