package com.ejemplo.model;

import java.time.LocalDate;
import java.util.Map;

import com.ejemplo.service.Ao3ScraperService;
import com.ejemplo.util.JsonRequestUtil;

public class ImportarFanficModel {

    private final FanficDAO fanficDAO = new FanficDAO();
    private final Ao3ScraperService ao3ScraperService = new Ao3ScraperService();

    public Fanfic importar(int userId, Map<String, Object> datos) {
        String url = JsonRequestUtil.obtenerTexto(datos, "url");
        String finishedDate = JsonRequestUtil.obtenerTexto(datos, "finishedDate");
        int userStars = JsonRequestUtil.obtenerEntero(datos, "userStars");

        validarEntrada(finishedDate, userStars);

        Fanfic fanfic = ao3ScraperService.extraerFanfic(url);

        if (fanficDAO.existePorUrl(userId, fanfic.getAo3Url())) {
            throw new IllegalArgumentException("Ese fanfic ya esta guardado en tu cuenta");
        }

        fanfic.setFinishedDate(finishedDate);
        fanfic.setUserStars(userStars);
        fanficDAO.guardar(userId, fanfic);
        return fanfic;
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
}
