package com.ejemplo.controller;

import java.io.IOException;
import java.util.Map;

import com.ejemplo.model.FanficDAO;
import com.ejemplo.model.StatsDAO;
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

        Integer userId = AccessControlUtil.requireLoggedUser(
                request, response, gson, "Necesitas iniciar sesion para ver tus estadisticas");
        if (userId == null) {
            return;
        }

        if (!AccessControlUtil.requireStandardUser(
                request, response, gson, "La cuenta admin solo puede usar el panel de administracion")) {
            return;
        }

        try {
            schemaInitializer.ensureSchema();
            int totalFanfics = fanficDAO.contarFanfics(userId);
            Map<String, Object> respuesta = new java.util.HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("totalFanfics", totalFanfics);

            if (totalFanfics < MINIMO_PARA_STATS) {
                respuesta.put("enabled", false);
                respuesta.put("mensaje", "Necesitas al menos 10 fanfics para desbloquear las estadisticas");
            } else {
                respuesta.put("enabled", true);
                respuesta.put("topRelationships", statsDAO.topRelationships(userId, 5));
                respuesta.put("topFandoms", statsDAO.topFandoms(userId, 5));
                respuesta.put("topWarnings", statsDAO.topWarnings(userId, 5));
                respuesta.put("ao3Ratings", statsDAO.ratingAo3Breakdown(userId));
                respuesta.put("userStars", statsDAO.estrellasBreakdown(userId));
                respuesta.put("categories", statsDAO.categoriesBreakdown(userId));
                respuesta.put("averageWords", statsDAO.mediaPalabras(userId));
            }
            ServletResponseUtil.writeJson(response, gson, respuesta);
        } catch (Exception e) {
            Map<String, Object> error = ServletResponseUtil.crearError("No se pudieron cargar las estadisticas");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            ServletResponseUtil.writeJson(response, gson, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    }
}
