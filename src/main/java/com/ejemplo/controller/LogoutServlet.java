package com.ejemplo.controller;

import java.io.IOException;

import com.ejemplo.model.LogoutModel;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/auth/logout")
public class LogoutServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final LogoutModel logoutModel = new LogoutModel();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(gson.toJson(logoutModel.cerrar(request)));
    }
}
