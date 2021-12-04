package fr.imtatlantique.simulation.Service;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;


@Getter
@Setter
public class HttpService {
    // init HttpClient
    private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private String serviceURL;

    public HttpService() {
        this.serviceURL = "http://localhost:8080";
    }

    public HttpService(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    // this method is used when adding neighbors, update the matrix of weights
    // and when we propagate messages in receiveAdd and receiveDel
    public HttpRequest preparePostRequest(String route, String inputJson) {
        return HttpRequest.newBuilder(URI.create(this.serviceURL+ route))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(inputJson)).build();
    }

    // this method is used only when a server execute a add or del operation
    public HttpRequest preparePostRequestWithouBody(String route) {
        return HttpRequest.newBuilder(URI.create(this.serviceURL+ route))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("")).build();
    }

    public CompletableFuture<HttpResponse<String>> sendPostRequest(HttpRequest request) {
        return this.client.sendAsync(request,HttpResponse.BodyHandlers.ofString());
    }
}
