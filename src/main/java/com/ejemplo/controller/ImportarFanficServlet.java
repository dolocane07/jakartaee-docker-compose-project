package com.ejemplo.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

import com.ejemplo.model.Fanfic;
import com.ejemplo.model.FanficDAO;
import com.ejemplo.service.Ao3ScraperService;
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
            String finishedDate = JsonRequestUtil.obtenerTexto(datos, "finishedDate");
            int userStars = JsonRequestUtil.obtenerEntero(datos, "userStars");

            if (finishedDate.isBlank()) {
                throw new IllegalArgumentException("La fecha en que lo terminaste es obligatoria");
            }

            LocalDate.parse(finishedDate);

            if (userStars < 1 || userStars > 5) {
                throw new IllegalArgumentException("Las estrellas deben estar entre 1 y 5");
            }

            Fanfic fanfic = ao3ScraperService.extraerFanfic(JsonRequestUtil.obtenerTexto(datos, "url"));

            if (fanficDAO.existePorUrl(userId, fanfic.getAo3Url())) {
                throw new IllegalArgumentException("Ese fanfic ya esta guardado en tu cuenta");
            }

            fanfic.setFinishedDate(finishedDate);
            fanfic.setUserStars(userStars);
            fanficDAO.guardar(userId, fanfic);

            ServletResponseUtil.writeJson(response, gson, Map.of("ok", true, "fanfic", fanfic));
        } catch (IllegalArgumentException e) {
            ServletResponseUtil.writeError(response, gson, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            Map<String, Object> error = ServletResponseUtil.crearError("No se pudo importar el fanfic");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            ServletResponseUtil.writeJson(response, gson, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    }
}
