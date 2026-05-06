package com.ejemplo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ejemplo.model.FanficDAO;
import com.ejemplo.model.StatsDAO;
import com.ejemplo.service.SchemaInitializer;
import com.ejemplo.util.ErrorUtil;
import com.ejemplo.util.SessionUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/estadisticas")
public class EstadisticasServlet extends HttpServlet {

    private static final int MINIMO_PARA_STATS = 10;

    private final Gson gson = new Gson();
    private final FanficDAO fanficDAO = new FanficDAO();
    private final StatsDAO statsDAO = new StatsDAO();
    private final SchemaInitializer schemaInitializer = new SchemaInitializer();

    @Override
    @SuppressWarnings("UseSpecificCatch")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        Integer userId = SessionUtil.getUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(gson.toJson(crearError("Necesitas iniciar sesion para ver tus estadisticas")));
            return;
        }

        if (SessionUtil.isAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(crearError("La cuenta admin solo puede usar el panel de administracion")));
            return;
        }

        try {
            schemaInitializer.ensureSchema();

            int totalFanfics = fanficDAO.contarFanfics(userId);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("totalFanfics", totalFanfics);

            if (totalFanfics < MINIMO_PARA_STATS) {
                respuesta.put("enabled", false);
                respuesta.put("mensaje", "Necesitas al menos 10 fanfics para desbloquear las estadisticas");
                response.getWriter().write(gson.toJson(respuesta));
                return;
            }

            respuesta.put("enabled", true);
            respuesta.put("topRelationships", statsDAO.topRelationships(userId, 5));
            respuesta.put("topFandoms", statsDAO.topFandoms(userId, 5));
            respuesta.put("topWarnings", statsDAO.topWarnings(userId, 5));
            respuesta.put("ao3Ratings", statsDAO.ratingAo3Breakdown(userId));
            respuesta.put("userStars", statsDAO.estrellasBreakdown(userId));
            respuesta.put("categories", statsDAO.categoriesBreakdown(userId));
            respuesta.put("averageWords", statsDAO.mediaPalabras(userId));

            response.getWriter().write(gson.toJson(respuesta));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> error = crearError("No se pudieron cargar las estadisticas");
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
