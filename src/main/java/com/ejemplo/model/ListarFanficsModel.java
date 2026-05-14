package com.ejemplo.model;

import java.util.HashMap;
import java.util.Map;

public class ListarFanficsModel {

    private final FanficDAO fanficDAO = new FanficDAO();

    public Map<String, Object> listar(int userId) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ok", true);
        respuesta.put("fanfics", fanficDAO.listarTodos(userId));
        return respuesta;
    }
}
