package com.ejemplo.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ejemplo.model.Fanfic;
import com.ejemplo.model.FanficDAO;
import com.ejemplo.service.SchemaInitializer;
import com.ejemplo.util.AccessControlUtil;
import com.ejemplo.util.ErrorUtil;
import com.ejemplo.util.JsonRequestUtil;
import com.ejemplo.util.ServletResponseUtil;
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

        Integer userId = AccessControlUtil.requireLoggedUser(
                request, response, gson, "Necesitas iniciar sesion para guardar fanfics");
        if (userId == null) {
            return;
        }

        if (!AccessControlUtil.requireStandardUser(
                request, response, gson, "La cuenta admin no puede importar fanfics")) {
            return;
        }

        try {
            schemaInitializer.ensureSchema();
            Map<String, Object> datos = JsonRequestUtil.leerJson(request, gson);
            Fanfic fanfic = new Fanfic();
            fanfic.setAo3Url(JsonRequestUtil.obtenerTexto(datos, "url"));
            fanfic.setAo3WorkId(extraerWorkId(fanfic.getAo3Url()));
            fanfic.setTitulo(JsonRequestUtil.obtenerTexto(datos, "titulo"));
            fanfic.setAutor(JsonRequestUtil.obtenerTexto(datos, "autor"));
            fanfic.setAo3Rating(JsonRequestUtil.obtenerTexto(datos, "ao3Rating"));
            fanfic.setWordCount(JsonRequestUtil.obtenerEntero(datos, "wordCount"));
            fanfic.setFinishedDate(JsonRequestUtil.obtenerTexto(datos, "finishedDate"));
            fanfic.setUserStars(JsonRequestUtil.obtenerEntero(datos, "userStars"));
            fanfic.setWarnings(parsearLista(JsonRequestUtil.obtenerTexto(datos, "warnings")));
            fanfic.setRelationships(parsearLista(JsonRequestUtil.obtenerTexto(datos, "relationships")));
            fanfic.setFandoms(parsearLista(JsonRequestUtil.obtenerTexto(datos, "fandoms")));
            fanfic.setCategories(parsearLista(JsonRequestUtil.obtenerTexto(datos, "categories")));

            validarFanficManual(fanfic);

            if (fanficDAO.existePorUrl(userId, fanfic.getAo3Url())) {
                throw new IllegalArgumentException("Ese fanfic ya esta guardado en tu cuenta");
            }

            fanficDAO.guardar(userId, fanfic);

            ServletResponseUtil.writeJson(response, gson, Map.of("ok", true, "fanfic", fanfic));
        } catch (IllegalArgumentException e) {
            ServletResponseUtil.writeError(response, gson, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            Map<String, Object> error = ServletResponseUtil.crearError("No se pudo guardar el fanfic manualmente");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            ServletResponseUtil.writeJson(response, gson, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
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
}
