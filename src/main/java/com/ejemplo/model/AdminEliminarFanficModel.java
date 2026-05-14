package com.ejemplo.model;

import java.util.Map;

import com.ejemplo.util.JsonRequestUtil;

public class AdminEliminarFanficModel {

    private final FanficDAO fanficDAO = new FanficDAO();

    public void eliminar(Map<String, Object> datos) {
        int fanficId = JsonRequestUtil.obtenerEntero(datos, "fanficId");
        fanficDAO.eliminarComoAdmin(fanficId);
    }
}
