package com.ejemplo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ejemplo.util.SessionUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/auth/logout")
public class LogoutServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        SessionUtil.cerrarSesion(request);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ok", true);
        response.getWriter().write(gson.toJson(respuesta));
    }
}
