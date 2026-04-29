package com.ejemplo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ejemplo.model.UserDAO;
import com.ejemplo.service.SchemaInitializer;
import com.ejemplo.util.ErrorUtil;
import com.ejemplo.util.SessionUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/admin/users")
public class AdminUsersServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final UserDAO userDAO = new UserDAO();
    private final SchemaInitializer schemaInitializer = new SchemaInitializer();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        if (!SessionUtil.isAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(crearError("Solo el admin puede ver los usuarios")));
            return;
        }

        try {
            schemaInitializer.ensureSchema();

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("users", userDAO.listarUsuariosConTotales());
            response.getWriter().write(gson.toJson(respuesta));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = crearError("No se pudieron cargar los usuarios");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            response.getWriter().write(gson.toJson(error));
        }
    }

    private Map<String, Object> crearError(String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("ok", false);
        error.put("mensaje", mensaje);
        return error;
    }
}
