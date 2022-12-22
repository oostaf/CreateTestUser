import org.apache.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HttpService {

    private final HttpClient httpClient;
    Properties properties;

    public HttpService() {
        this.properties = getProperties();
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    /**
     * Sends POST request
     * @param urlPart - part of url that goes after base domain url
     * @param params - parameters with information to post
     * @return - HttpResponse with information
     * @throws CustomException
     */
    public HttpResponse<String> sendPostRequest(String urlPart, Map<Object, Object> params) throws CustomException {
        params.put("secretKey", getSecretKey());

        return sendPostRequest(urlPart, params, HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
    }

    /**
     * Sends GET request
     * @param urlPart - part of url that goes after base domain url
     * @return - HttpResponse with information
     * @throws CustomException
     */
    public HttpResponse<String> sendGet(String urlPart) throws CustomException {
        String baseUrl = properties.getProperty("http.url");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(baseUrl + urlPart))
                .setHeader("User-Agent", "Test")
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new CustomException("Fail to send GET request to +" + baseUrl + urlPart, e);
        }

        return response;
    }

    /**
     * Sends POST request
     * @param urlPart - part of url that goes after base domain url
     * @param params - parameters with information to post
     * @param headerName - header name to post
     * @param headerValue - header value to post
     * @return - HttpResponse with information
     * @throws CustomException
     */
    private HttpResponse<String> sendPostRequest(String urlPart, Map<Object, Object> params, String headerName, String headerValue) throws CustomException {
        String baseUrl = properties.getProperty("http.url");

        HttpRequest request = HttpRequest.newBuilder()
                .POST(dataToBodyPublisher(params))
                .uri(URI.create(baseUrl + urlPart))
                .setHeader("User-Agent", "Java 11 HttpClient")
                .header(headerName, headerValue)
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new CustomException("Fail to send POST request to +" + baseUrl + urlPart, e);
        }

        return response;
    }

    /**
     * Get user authorization secret key via POST request
     * @return String secret key
     * @throws CustomException
     */
    private String getSecretKey() throws CustomException {
        Map<Object, Object> data = new HashMap<>();
        data.put("username", properties.getProperty("http.user"));
        data.put("password", properties.getProperty("http.password"));

        return sendPostRequest("auth", data, HttpHeaders.AUTHORIZATION, "Basic Og==").body();
    }

    /**
     * Get properties from config.properties file
     * @return Properties
     */
    private Properties getProperties() {
        try (InputStream input = HttpService.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties properties = new Properties();
            if (input == null) {
                throw new RuntimeException("Unable to find properties file");
            }
            properties.load(input);
            return properties;
        } catch (IOException ex) {
            throw new RuntimeException("Unable to read from properties file", ex);
        }
    }

    /**
     * Parse Map with data into BodyPublisher to use in requests
     * @param data - Map with key value info
     * @return HttpRequest.BodyPublisher
     */
    private static HttpRequest.BodyPublisher dataToBodyPublisher(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

}
