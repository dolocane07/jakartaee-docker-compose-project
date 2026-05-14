package com.ejemplo.model;

import java.util.Map;

import com.ejemplo.util.JsonRequestUtil;
import com.ejemplo.util.PasswordUtil;

public class LoginUserModel {

    private final UserDAO userDAO = new UserDAO();

    public User autenticar(Map<String, Object> datos) {
        String identifier = JsonRequestUtil.obtenerTexto(datos, "identifier").toLowerCase();
        String password = JsonRequestUtil.obtenerTexto(datos, "password");

        if (identifier.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Completa usuario/email y contraseña");
        }

        User user = userDAO.buscarPorIdentifier(identifier);
        if (user == null || !PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            return null;
        }

        return user;
    }
}
