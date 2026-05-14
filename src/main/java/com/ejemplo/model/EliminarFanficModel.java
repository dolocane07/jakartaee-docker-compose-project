package com.ejemplo.model;

import java.util.Map;

import com.ejemplo.util.JsonRequestUtil;

public class EliminarFanficModel {

    private final FanficDAO fanficDAO = new FanficDAO();

    public void eliminar(int userId, Map<String, Object> datos) {
        int fanficId = JsonRequestUtil.obtenerEntero(datos, "fanficId");
        fanficDAO.eliminar(userId, fanficId);
    }
}
