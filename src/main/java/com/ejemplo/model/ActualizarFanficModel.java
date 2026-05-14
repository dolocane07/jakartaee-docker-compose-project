package com.ejemplo.model;

import java.time.LocalDate;
import java.util.Map;

import com.ejemplo.util.JsonRequestUtil;

public class ActualizarFanficModel {

    private final FanficDAO fanficDAO = new FanficDAO();

    public void actualizar(int userId, Map<String, Object> datos) {
        int fanficId = JsonRequestUtil.obtenerEntero(datos, "fanficId");
        String finishedDate = JsonRequestUtil.obtenerTexto(datos, "finishedDate");
        int userStars = JsonRequestUtil.obtenerEntero(datos, "userStars");

        LocalDate.parse(finishedDate);
        if (userStars < 1 || userStars > 5) {
            throw new IllegalArgumentException("Las estrellas deben estar entre 1 y 5");
        }

        fanficDAO.actualizarLectura(userId, fanficId, finishedDate, userStars);
    }
}
