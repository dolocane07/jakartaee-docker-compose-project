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

@WebServlet("/api/admin/fanfics")
public class AdminFanficsServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final FanficDAO fanficDAO = new FanficDAO();
    private final SchemaInitializer schemaInitializer = new SchemaInitializer();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        if (!AccessControlUtil.requireAdmin(
                request, response, gson, "Solo el admin puede ver las entradas de otros usuarios")) {
            return;
        }

        try {
            schemaInitializer.ensureSchema();
            ServletResponseUtil.writeJson(response, gson, Map.of("ok", true, "fanfics", fanficDAO.listarTodosAdmin()));
        } catch (Exception e) {
            Map<String, Object> error = ServletResponseUtil.crearError("No se pudieron cargar las entradas del admin");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            ServletResponseUtil.writeJson(response, gson, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    }
}
