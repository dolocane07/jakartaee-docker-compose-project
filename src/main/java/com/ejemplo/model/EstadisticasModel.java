package com.ejemplo.model;

import java.util.HashMap;
import java.util.Map;

public class EstadisticasModel {

    private static final int MINIMO_PARA_STATS = 10;

    private final FanficDAO fanficDAO = new FanficDAO();
    private final StatsDAO statsDAO = new StatsDAO();

    public Map<String, Object> obtener(int userId) {
        int totalFanfics = fanficDAO.contarFanfics(userId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ok", true);
        respuesta.put("totalFanfics", totalFanfics);

        if (totalFanfics < MINIMO_PARA_STATS) {
            respuesta.put("enabled", false);
            respuesta.put("mensaje", "Necesitas al menos 10 fanfics para desbloquear las estadisticas");
            return respuesta;
        }

        respuesta.put("enabled", true);
        respuesta.put("topRelationships", statsDAO.topRelationships(userId, 5));
        respuesta.put("topFandoms", statsDAO.topFandoms(userId, 5));
        respuesta.put("topWarnings", statsDAO.topWarnings(userId, 5));
        respuesta.put("ao3Ratings", statsDAO.ratingAo3Breakdown(userId));
        respuesta.put("userStars", statsDAO.estrellasBreakdown(userId));
        respuesta.put("categories", statsDAO.categoriesBreakdown(userId));
        respuesta.put("averageWords", statsDAO.mediaPalabras(userId));
        return respuesta;
    }
}
