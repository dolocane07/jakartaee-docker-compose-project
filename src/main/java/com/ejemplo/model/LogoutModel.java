package com.ejemplo.model;

import java.util.HashMap;
import java.util.Map;

import com.ejemplo.util.SessionUtil;

import jakarta.servlet.http.HttpServletRequest;

public class LogoutModel {

    public Map<String, Object> cerrar(HttpServletRequest request) {
        SessionUtil.cerrarSesion(request);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ok", true);
        return respuesta;
    }
}
