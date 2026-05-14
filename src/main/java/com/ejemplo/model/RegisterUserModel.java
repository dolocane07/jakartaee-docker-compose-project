package com.ejemplo.model;

import java.util.Map;

import com.ejemplo.util.JsonRequestUtil;

public class RegisterUserModel {

    private final UserDAO userDAO = new UserDAO();

    public User registrar(Map<String, Object> datos) {
        String username = JsonRequestUtil.obtenerTexto(datos, "username").toLowerCase();
        String email = JsonRequestUtil.obtenerTexto(datos, "email").toLowerCase();
        String password = JsonRequestUtil.obtenerTexto(datos, "password");

        validarRegistro(username, email, password);

        if (userDAO.existeUsername(username)) {
            throw new IllegalArgumentException("Ese nombre de usuario ya existe");
        }

        if (userDAO.existeEmail(email)) {
            throw new IllegalArgumentException("Ese email ya esta registrado");
        }

        return userDAO.registrar(username, email, password);
    }

    private void validarRegistro(String username, String email, String password) {
        if (username.length() < 3 || username.length() > 30) {
            throw new IllegalArgumentException("El usuario debe tener entre 3 y 30 caracteres");
        }

        if (!username.matches("[a-z0-9._-]+")) {
            throw new IllegalArgumentException("El usuario solo puede llevar letras minusculas, numeros, punto, guion y guion bajo");
        }

        if (email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Introduce un email valido");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
    }
}
