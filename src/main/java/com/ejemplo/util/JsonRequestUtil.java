package com.ejemplo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;

public final class JsonRequestUtil {

    private JsonRequestUtil() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> leerJson(HttpServletRequest request, Gson gson) throws IOException {
        StringBuilder json = new StringBuilder();
        BufferedReader reader = request.getReader();
        String linea;

        while ((linea = reader.readLine()) != null) {
            json.append(linea);
        }

        return gson.fromJson(json.toString(), Map.class);
    }

    public static String obtenerTexto(Map<String, Object> datos, String clave) {
        if (datos == null || datos.get(clave) == null) {
            return "";
        }
        return String.valueOf(datos.get(clave)).trim();
    }

    public static int obtenerEntero(Map<String, Object> datos, String clave) {
        if (datos == null || datos.get(clave) == null) {
            return 0;
        }

        Object valor = datos.get(clave);
        if (valor instanceof Number numero) {
            return numero.intValue();
        }

        return Integer.parseInt(String.valueOf(valor).trim());
    }
}
