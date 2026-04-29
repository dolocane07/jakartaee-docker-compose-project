package com.ejemplo.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.ejemplo.model.FanficDAO;
import com.ejemplo.service.SchemaInitializer;
import com.ejemplo.util.ErrorUtil;
import com.ejemplo.util.SessionUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/admin/fanfics/eliminar")
public class AdminEliminarFanficServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final FanficDAO fanficDAO = new FanficDAO();
    private final SchemaInitializer schemaInitializer = new SchemaInitializer();

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        if (!SessionUtil.isAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(crearError("Solo el admin puede borrar entradas ajenas")));
            return;
        }

        try {
            schemaInitializer.ensureSchema();

            StringBuilder json = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;

            while ((linea = reader.readLine()) != null) {
                json.append(linea);
            }

            Map<String, Object> datos = gson.fromJson(json.toString(), Map.class);
            int fanficId = obtenerEntero(datos, "fanficId");

            fanficDAO.eliminarComoAdmin(fanficId);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("mensaje", "Entrada borrada por admin");
            response.getWriter().write(gson.toJson(respuesta));
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(crearError(e.getMessage())));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = crearError("No se pudo borrar la entrada como admin");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            response.getWriter().write(gson.toJson(error));
        }
    }

    private int obtenerEntero(Map<String, Object> datos, String clave) {
        if (datos == null || datos.get(clave) == null) {
            return 0;
        }

        Object valor = datos.get(clave);
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        return Integer.parseInt(String.valueOf(valor));
    }

    private Map<String, Object> crearError(String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("ok", false);
        error.put("mensaje", mensaje);
        return error;
    }
}
