package com.ejemplo.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.ejemplo.model.UserDAO;
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

@WebServlet("/api/admin/users/eliminar")
public class AdminEliminarUsuarioServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final UserDAO userDAO = new UserDAO();
    private final SchemaInitializer schemaInitializer = new SchemaInitializer();

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        if (!AccessControlUtil.requireAdmin(request, response, gson, "Solo el admin puede borrar cuentas")) {
            return;
        }

        Integer adminUserId = AccessControlUtil.requireLoggedUser(
                request, response, gson, "Necesitas iniciar sesion como admin");
        if (adminUserId == null) {
            return;
        }

        try {
            schemaInitializer.ensureSchema();
            Map<String, Object> datos = JsonRequestUtil.leerJson(request, gson);
            userDAO.eliminarCuentaComoAdmin(JsonRequestUtil.obtenerEntero(datos, "userId"), adminUserId);

            ServletResponseUtil.writeJson(response, gson, Map.of("ok", true, "mensaje", "Cuenta borrada por admin"));
        } catch (IllegalArgumentException e) {
            ServletResponseUtil.writeError(response, gson, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            Map<String, Object> error = ServletResponseUtil.crearError("No se pudo borrar la cuenta");
            error.put("detalle", ErrorUtil.getRootCauseMessage(e));
            ServletResponseUtil.writeJson(response, gson, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    }
}
