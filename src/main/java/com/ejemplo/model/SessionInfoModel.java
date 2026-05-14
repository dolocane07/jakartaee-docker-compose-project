package com.ejemplo.model;

import java.util.HashMap;
import java.util.Map;

import com.ejemplo.util.SessionUtil;

import jakarta.servlet.http.HttpServletRequest;

public class SessionInfoModel {

    public Map<String, Object> obtenerRespuesta(HttpServletRequest request) {
        Map<String, Object> respuesta = new HashMap<>();
        Map<String, Object> user = SessionUtil.getUserData(request);
        respuesta.put("ok", true);
        respuesta.put("loggedIn", user != null);
        respuesta.put("user", user);
        return respuesta;
    }
}
