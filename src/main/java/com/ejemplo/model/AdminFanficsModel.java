package com.ejemplo.model;

import java.util.HashMap;
import java.util.Map;

public class AdminFanficsModel {

    private final FanficDAO fanficDAO = new FanficDAO();

    public Map<String, Object> listar() {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ok", true);
        respuesta.put("fanfics", fanficDAO.listarTodosAdmin());
        return respuesta;
    }
}
