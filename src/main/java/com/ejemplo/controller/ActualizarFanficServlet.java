package com.ejemplo.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.ejemplo.model.FanficDAO;
import com.ejemplo.service.SchemaInitializer;
import com.ejemplo.util.ErrorUtil;
import com.ejemplo.util.SessionUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/fanfics/actualizar")
public class ActualizarFanficServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final FanficDAO fanficDAO = new FanficDAO();
    private final SchemaInitializer schemaInitializer = new SchemaInitializer();

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        Integer userId = SessionUtil.getUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(gson.toJson(crearError("Necesitas iniciar sesion para editar entradas")));
            return;
        }

        try {
            schemaInitializer.ensureSchema();

            Map<String, Object> datos = leerJson(request);
            int fanficId = obtenerEntero(datos, "fanficId");
            String finishedDate = obtenerTexto(datos, "finishedDate");
            int userStars = obtenerEntero(datos, "userStars");

            LocalDate.parse(finishedDate);
            if (userStars < 1 || userStars > 5) {
                throw new IllegalArgumentException("Las estrellas deben estar entre 1 y 5");
            }

            fanficDAO.actualizarLectura(userId, fanficId, finishedDate, userStars);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("mensaje", "Entrada actualizada");
            response.getWriter().write(gson.toJson(respuesta));
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(crearError(e.getMessage())));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = crearError("No se pudo actualizar la entrada");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            response.getWriter().write(gson.toJson(error));
        }
    }

    private Map<String, Object> leerJson(HttpServletRequest request) throws IOException {
        StringBuilder json = new StringBuilder();
        BufferedReader reader = request.getReader();
        String linea;

        while ((linea = reader.readLine()) != null) {
            json.append(linea);
        }

        return gson.fromJson(json.toString(), Map.class);
    }

    private String obtenerTexto(Map<String, Object> datos, String clave) {
        if (datos == null || datos.get(clave) == null) {
            return "";
        }
        return String.valueOf(datos.get(clave)).trim();
    }

    private int obtenerEntero(Map<String, Object> datos, String clave) {
        if (datos == null || datos.get(clave) == null) {
            return 0;
        }

        Object valor = datos.get(clave);
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        return Integer.parseInt(String.valueOf(valor));
    }

    private Map<String, Object> crearError(String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("ok", false);
        error.put("mensaje", mensaje);
        return error;
    }
}
