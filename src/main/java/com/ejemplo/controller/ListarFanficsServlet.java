package com.ejemplo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ejemplo.model.FanficDAO;
import com.ejemplo.service.SchemaInitializer;
import com.ejemplo.util.ErrorUtil;
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
    @SuppressWarnings({"CallToPrintStackTrace", "UseSpecificCatch"})
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            schemaInitializer.ensureSchema();

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("fanfics", fanficDAO.listarTodos());

            response.getWriter().write(gson.toJson(respuesta));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "No se pudieron cargar los fanfics");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));

            response.getWriter().write(gson.toJson(error));
        }
    }
}
