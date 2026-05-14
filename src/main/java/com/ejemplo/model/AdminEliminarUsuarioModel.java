package com.ejemplo.model;

import java.util.Map;

import com.ejemplo.util.JsonRequestUtil;

public class AdminEliminarUsuarioModel {

    private final UserDAO userDAO = new UserDAO();

    public void eliminar(int adminUserId, Map<String, Object> datos) {
        int userId = JsonRequestUtil.obtenerEntero(datos, "userId");
        userDAO.eliminarCuentaComoAdmin(userId, adminUserId);
    }
}
