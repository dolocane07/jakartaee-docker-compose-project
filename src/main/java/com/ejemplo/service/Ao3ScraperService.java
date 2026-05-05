package com.ejemplo.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;

import org.jsoup.Jsoup;
import org.jsoup.HttpStatusException;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.ejemplo.model.Fanfic;

public class Ao3ScraperService {

    private static final Pattern WORK_ID_PATTERN = Pattern.compile("/works/(\\d+)");
    private static final int MAX_REINTENTOS = 3;

    public Fanfic extraerFanfic(String urlOriginal) {
        String urlNormalizada = normalizarUrl(urlOriginal);
        Document documento = obtenerDocumentoConFallback(urlNormalizada);

        Fanfic fanfic = new Fanfic();
        fanfic.setAo3Url(urlNormalizada);
        fanfic.setAo3WorkId(extraerWorkId(urlNormalizada));
        fanfic.setTitulo(extraerTitulo(documento));
        fanfic.setAutor(extraerAutor(documento));
        fanfic.setAo3Rating(extraerTexto(documento, "dd.rating.tags li a.tag", "Sin clasificar"));
        fanfic.setWordCount(extraerEntero(documento, "dd.words", 0));
        fanfic.setWarnings(extraerTags(documento, "dd.warning.tags li a.tag"));
        fanfic.setRelationships(extraerTags(documento, "dd.relationship.tags li a.tag"));
        fanfic.setFandoms(extraerTags(documento, "dd.fandom.tags li a.tag"));
        fanfic.setCategories(extraerTags(documento, "dd.category.tags li a.tag"));

        if (fanfic.getTitulo().isBlank()) {
            throw new IllegalArgumentException("No se pudo leer el titulo del fanfic desde AO3");
        }

        return fanfic;
    }

    private String normalizarUrl(String urlOriginal) {
        if (urlOriginal == null || urlOriginal.isBlank()) {
            throw new IllegalArgumentException("La URL es obligatoria");
        }

        URI uri = URI.create(urlOriginal.trim());
        String host = uri.getHost();

        if (host == null || !host.contains("archiveofourown.org")) {
            throw new IllegalArgumentException("La URL debe pertenecer a archiveofourown.org");
        }

        Matcher matcher = WORK_ID_PATTERN.matcher(uri.getPath());
        if (!matcher.find()) {
            throw new IllegalArgumentException("La URL debe apuntar a un work de AO3");
        }

        return "https://archiveofourown.org/works/" + matcher.group(1);
    }

    private String extraerWorkId(String url) {
        Matcher matcher = WORK_ID_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private Document obtenerDocumentoConFallback(String urlNormalizada) {
        List<String> urls = new ArrayList<>();
        urls.add(urlNormalizada);
        urls.add(anadirVistaAdulta(urlNormalizada));
        urls.add(urlNormalizada + "?view_adult=true&view_full_work=true");

        String ultimoMensaje = "No se pudo acceder a AO3";

        for (String url : urls) {
            for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
                try {
                    Document documento = obtenerDocumento(url);
                    if (paginaPareceWork(documento)) {
                        return documento;
                    }
                    ultimoMensaje = "AO3 devolvio una pagina sin los metadatos esperados";
                } catch (HttpStatusException e) {
                    ultimoMensaje = construirMensajeEstado(e);

                    if (!esErrorReintentable(e.getStatusCode())) {
                        throw new RuntimeException(ultimoMensaje, e);
                    }
                } catch (UnsupportedMimeTypeException e) {
                    throw new RuntimeException("AO3 devolvio un tipo de contenido inesperado", e);
                } catch (IOException e) {
                    ultimoMensaje = "No se pudo acceder a AO3: " + e.getMessage();
                }

                esperarAntesDeReintentar(intento);
            }
        }

        throw new RuntimeException(ultimoMensaje);
    }

    private Document obtenerDocumento(String url) throws IOException {
        try {
            return obtenerDocumentoConJsoup(url);
        } catch (SSLHandshakeException e) {
            return obtenerDocumentoConHttpClient(url, e);
        } catch (IOException e) {
            if (contieneHandshakeFailure(e)) {
                return obtenerDocumentoConHttpClient(url, e);
            }
            throw e;
        }
    }

    private Document obtenerDocumentoConJsoup(String url) throws IOException {
        Response response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
                .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                .referrer("https://archiveofourown.org/")
                .followRedirects(true)
                .timeout(20000)
                .execute();

        return response.parse();
    }

    private Document obtenerDocumentoConHttpClient(String url, Exception causaOriginal) throws IOException {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, new SecureRandom());

            SSLParameters sslParameters = new SSLParameters();
            sslParameters.setProtocols(new String[] {"TLSv1.3", "TLSv1.2"});

            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(20))
                    .sslContext(sslContext)
                    .sslParameters(sslParameters)
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
                    .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                    .header("Referer", "https://archiveofourown.org/")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new HttpStatusException("HTTP error fetching URL", response.statusCode(), url);
            }

            return Jsoup.parse(response.body(), url);
        } catch (HttpStatusException e) {
            throw e;
        } catch (Exception e) {
            IOException io = new IOException("No se pudo acceder a AO3 tras el fallback TLS: " + e.getMessage(), e);
            io.addSuppressed(causaOriginal);
            throw io;
        }
    }

    private boolean contieneHandshakeFailure(IOException e) {
        String mensaje = e.getMessage();
        return mensaje != null && mensaje.toLowerCase().contains("handshake_failure");
    }

    private boolean paginaPareceWork(Document documento) {
        return !extraerTitulo(documento).isBlank() && documento.selectFirst("dd.words") != null;
    }

    private String anadirVistaAdulta(String url) {
        return url + "?view_adult=true";
    }

    private boolean esErrorReintentable(int statusCode) {
        return statusCode == 403 || statusCode == 429 || statusCode == 500 || statusCode == 502
                || statusCode == 503 || statusCode == 504 || statusCode == 520 || statusCode == 521
                || statusCode == 522 || statusCode == 523 || statusCode == 524 || statusCode == 525;
    }

    private String construirMensajeEstado(HttpStatusException e) {
        if (e.getStatusCode() == 525) {
            return "AO3 devolvio un error 525 de Cloudflare. Suele ser temporal y ocurre a veces con scrapers en servidores como Railway. Prueba otra vez en unos minutos.";
        }

        return "AO3 devolvio HTTP " + e.getStatusCode() + " al intentar leer el fanfic";
    }

    private void esperarAntesDeReintentar(int intento) {
        try {
            long pausa = 400L * intento;
            Thread.sleep(pausa);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String extraerTitulo(Document documento) {
        Element titulo = documento.selectFirst("h2.title.heading");
        return titulo != null ? titulo.text().trim() : "";
    }

    private String extraerAutor(Document documento) {
        Element autor = documento.selectFirst("a[rel=author]");
        return autor != null ? autor.text().trim() : "Autor desconocido";
    }

    private String extraerTexto(Document documento, String selector, String porDefecto) {
        Element elemento = documento.selectFirst(selector);
        return elemento != null ? elemento.text().trim() : porDefecto;
    }

    private int extraerEntero(Document documento, String selector, int porDefecto) {
        Element elemento = documento.selectFirst(selector);
        if (elemento == null) {
            return porDefecto;
        }

        String texto = elemento.text().replace(",", "").trim();
        if (texto.isBlank()) {
            return porDefecto;
        }

        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    private List<String> extraerTags(Document documento, String selector) {
        return documento.select(selector)
                .stream()
                .map(Element::text)
                .map(String::trim)
                .filter(texto -> !texto.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }
}
