package com.ejemplo.model;

import java.util.HashMap;
import java.util.Map;

public class AdminUsersModel {

    private final UserDAO userDAO = new UserDAO();

    public Map<String, Object> listar() {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ok", true);
        respuesta.put("users", userDAO.listarUsuariosConTotales());
        return respuesta;
    }
}
