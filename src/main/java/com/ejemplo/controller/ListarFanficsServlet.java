package com.ejemplo.controller;

import java.io.IOException;
import java.util.Map;

import com.ejemplo.model.FanficDAO;
import com.ejemplo.service.SchemaInitializer;
import com.ejemplo.util.AccessControlUtil;
import com.ejemplo.util.ErrorUtil;
import com.ejemplo.util.ServletResponseUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/fanfics")
public class ListarFanficsServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final FanficDAO fanficDAO = new FanficDAO();
    private final SchemaInitializer schemaInitializer = new SchemaInitializer();

    @Override
    @SuppressWarnings("UseSpecificCatch")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        Integer userId = AccessControlUtil.requireLoggedUser(
                request, response, gson, "Necesitas iniciar sesion para ver tu biblioteca");
        if (userId == null) {
            return;
        }

        if (!AccessControlUtil.requireStandardUser(
                request, response, gson, "La cuenta admin solo puede usar el panel de administracion")) {
            return;
        }

        try {
            schemaInitializer.ensureSchema();
            ServletResponseUtil.writeJson(response, gson, Map.of("ok", true, "fanfics", fanficDAO.listarTodos(userId)));
        } catch (Exception e) {
            Map<String, Object> error = ServletResponseUtil.crearError("No se pudieron cargar los fanfics");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            ServletResponseUtil.writeJson(response, gson, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    }
}
