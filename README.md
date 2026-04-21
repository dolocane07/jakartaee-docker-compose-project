# Proyecto Jakarta EE + Docker Compose + MySQL + phpMyAdmin

Este proyecto incluye:

- **Servidor**: Tomcat 10 (compatible con Jakarta Servlet)
- **Maven**: compilación del proyecto Java en una imagen Docker multietapa
- **Frontend**: HTML, CSS y JavaScript
- **Backend**: Servlet + clases Java
- **Conector MySQL**: MySQL Connector/J
- **Base de datos**: MySQL 8
- **Gestor visual**: phpMyAdmin

## Estructura

```bash
jakartaee-docker-compose-project/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── README.md
├── mysql/
│   └── init/
│       └── 01-bd1.sql
└── src/
    └── main/
        ├── java/
        │   └── com/ejemplo/
        │       ├── controller/
        │       │   └── BuscarContactosServlet.java
        │       └── model/
        │           ├── ConexionBD.java
        │           ├── Contacto.java
        │           └── ContactoDAO.java
        └── webapp/
            ├── css/
            │   └── estilos.css
            ├── js/
            │   └── app.js
            ├── WEB-INF/
            │   └── web.xml
            └── index.html
```

## Puesta en marcha

Desde la carpeta del proyecto:

```bash
docker compose up --build
```

## URLs

- Aplicación web: `http://localhost:8080`
- phpMyAdmin: `http://localhost:8081`
  - Usuario: `root`
  - Contraseña: `root`
- MySQL desde el host: `localhost:3307`
  - Base de datos: `bd1`
  - Usuario: `root`
  - Contraseña: `root`

## Funcionamiento

1. El usuario escribe un nombre en el buscador.
2. El frontend envía una petición `POST` con `fetch` y JSON al servlet:
   - `/api/buscar-contactos`
3. El servlet recibe el JSON, invoca al DAO y consulta MySQL.
4. La respuesta vuelve en JSON al navegador.
5. JavaScript pinta los resultados en pantalla.

## Ejemplo de JSON enviado

```json
{
  "texto": "Ana"
}
```

## Ejemplo de JSON devuelto

```json
{
  "ok": true,
  "total": 1,
  "resultados": [
    {
      "ideCon": 1,
      "nomCon": "Ana López",
      "tlfCon": 600111222
    }
  ]
}
```
