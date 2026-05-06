package com.ejemplo.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.ejemplo.model.Fanfic;
import com.ejemplo.model.FanficDAO;
import com.ejemplo.service.Ao3ScraperService;
import com.ejemplo.service.SchemaInitializer;
import com.ejemplo.util.ErrorUtil;
import com.ejemplo.util.SessionUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/fanfics/importar")
public class ImportarFanficServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final FanficDAO fanficDAO = new FanficDAO();
    private final Ao3ScraperService ao3ScraperService = new Ao3ScraperService();
    private final SchemaInitializer schemaInitializer = new SchemaInitializer();

    @Override
    @SuppressWarnings({"unchecked", "UseSpecificCatch"})
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
            String url = obtenerTexto(datos, "url");
            String finishedDate = obtenerTexto(datos, "finishedDate");
            int userStars = obtenerEntero(datos, "userStars");

            validarEntrada(finishedDate, userStars);

            Fanfic fanfic = ao3ScraperService.extraerFanfic(url);

            if (fanficDAO.existePorUrl(userId, fanfic.getAo3Url())) {
                throw new IllegalArgumentException("Ese fanfic ya esta guardado en tu cuenta");
            }

            fanfic.setFinishedDate(finishedDate);
            fanfic.setUserStars(userStars);
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

            Map<String, Object> error = crearError("No se pudo importar el fanfic");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            response.getWriter().write(gson.toJson(error));
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

        return Integer.parseInt(String.valueOf(valor));
    }

    private void validarEntrada(String finishedDate, int userStars) {
        if (finishedDate.isBlank()) {
            throw new IllegalArgumentException("La fecha en que lo terminaste es obligatoria");
        }

        LocalDate.parse(finishedDate);

        if (userStars < 1 || userStars > 5) {
            throw new IllegalArgumentException("Las estrellas deben estar entre 1 y 5");
        }
    }

    private Map<String, Object> crearError(String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("ok", false);
        error.put("mensaje", mensaje);
        return error;
    }
}
