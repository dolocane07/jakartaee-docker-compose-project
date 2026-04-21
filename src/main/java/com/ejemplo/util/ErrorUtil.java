package com.ejemplo.util;

public final class ErrorUtil {

    private ErrorUtil() {
    }

    public static String getRootCauseMessage(Throwable error) {
        if (error == null) {
            return "Error desconocido";
        }

        Throwable actual = error;
        while (actual.getCause() != null && actual.getCause() != actual) {
            actual = actual.getCause();
        }

        String mensaje = actual.getMessage();
        return mensaje == null || mensaje.isBlank() ? actual.toString() : mensaje;
    }
}
