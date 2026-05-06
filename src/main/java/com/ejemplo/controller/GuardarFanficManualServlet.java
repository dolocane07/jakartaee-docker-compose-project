package com.ejemplo.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ejemplo.model.Fanfic;
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

@WebServlet("/api/fanfics/manual")
public class GuardarFanficManualServlet extends HttpServlet {

    private static final Pattern WORK_ID_PATTERN = Pattern.compile("/works/(\\d+)");

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
            response.getWriter().write(gson.toJson(crearError("Necesitas iniciar sesion para guardar fanfics")));
            return;
        }

        if (SessionUtil.isAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(crearError("La cuenta admin no puede importar fanfics")));
            return;
        }

        try {
            schemaInitializer.ensureSchema();

            StringBuilder jsonRecibido = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;

            while ((linea = reader.readLine()) != null) {
                jsonRecibido.append(linea);
            }

            Map<String, Object> datos = gson.fromJson(jsonRecibido.toString(), Map.class);

            Fanfic fanfic = new Fanfic();
            fanfic.setAo3Url(obtenerTexto(datos, "url"));
            fanfic.setAo3WorkId(extraerWorkId(fanfic.getAo3Url()));
            fanfic.setTitulo(obtenerTexto(datos, "titulo"));
            fanfic.setAutor(obtenerTexto(datos, "autor"));
            fanfic.setAo3Rating(obtenerTexto(datos, "ao3Rating"));
            fanfic.setWordCount(obtenerEntero(datos, "wordCount"));
            fanfic.setFinishedDate(obtenerTexto(datos, "finishedDate"));
            fanfic.setUserStars(obtenerEntero(datos, "userStars"));
            fanfic.setWarnings(parsearLista(obtenerTexto(datos, "warnings")));
            fanfic.setRelationships(parsearLista(obtenerTexto(datos, "relationships")));
            fanfic.setFandoms(parsearLista(obtenerTexto(datos, "fandoms")));
            fanfic.setCategories(parsearLista(obtenerTexto(datos, "categories")));

            validarFanficManual(fanfic);

            if (fanficDAO.existePorUrl(userId, fanfic.getAo3Url())) {
                throw new IllegalArgumentException("Ese fanfic ya esta guardado en tu cuenta");
            }

            fanficDAO.guardar(userId, fanfic);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("fanfic", fanfic);
            response.getWriter().write(gson.toJson(respuesta));
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(crearError(e.getMessage())));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = crearError("No se pudo guardar el fanfic manualmente");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            response.getWriter().write(gson.toJson(error));
        }
    }

    private void validarFanficManual(Fanfic fanfic) {
        if (fanfic.getAo3Url().isBlank()) {
            throw new IllegalArgumentException("La URL del fanfic es obligatoria");
        }

        if (fanfic.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El titulo es obligatorio");
        }

        if (fanfic.getAutor().isBlank()) {
            throw new IllegalArgumentException("El autor es obligatorio");
        }

        if (fanfic.getAo3Rating().isBlank()) {
            throw new IllegalArgumentException("El AO3 rating es obligatorio");
        }

        if (fanfic.getWordCount() < 0) {
            throw new IllegalArgumentException("El word count no puede ser negativo");
        }

        if (fanfic.getFinishedDate().isBlank()) {
            throw new IllegalArgumentException("La fecha en que lo terminaste es obligatoria");
        }

        LocalDate.parse(fanfic.getFinishedDate());

        if (fanfic.getUserStars() < 1 || fanfic.getUserStars() > 5) {
            throw new IllegalArgumentException("Las estrellas deben estar entre 1 y 5");
        }
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

        return Integer.parseInt(String.valueOf(valor).trim());
    }

    private List<String> parsearLista(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }

        return Arrays.stream(texto.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private String extraerWorkId(String url) {
        Matcher matcher = WORK_ID_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private Map<String, Object> crearError(String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("ok", false);
        error.put("mensaje", mensaje);
        return error;
    }
}
