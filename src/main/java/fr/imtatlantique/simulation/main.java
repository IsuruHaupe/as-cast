package fr.imtatlantique.simulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.imtatlantique.simulation.Controller.ServerController;
import fr.imtatlantique.simulation.Service.HttpService;
import fr.imtatlantique.simulation.Service.ServerService;
import fr.imtatlantique.simulation.Structures.*;
import fr.imtatlantique.simulation.Utils.JSONUtils;
import jdk.jfr.Threshold;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class main {
    private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private static final String serviceURL = "http://localhost:8080";
    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        // test serialization of data before sending http request
        /*
        // UNCOMMENT IF YOU WANT TO SEE HOW SERIALIZATION WORKS
        testDeserializationServer();
        testDeserializationMAdd();
        testDeserializationMDel();
        testDeserializationPayloadMAdd();
        testDeserializationPayloadMDel();
        testDeserializationMap();
        // launch two servers before running this command at port 8080 and 8081 with and id of 1 and 2
        testAjoutPuisSuppressionDeVoisin();

         */

        // launch the spring boot server
        ConfigurableApplicationContext context = SpringApplication.run(main.class, args);
        // retrieve server ID
        ServerController controller = context.getBean(ServerController.class);
        System.out.println("SERVER ID : " + controller.getServerService().getServerID());

        // UNCOMMENT WHEN YOU HAVE THREE SERVERS RUNNING (B, C, D)
        // DON'T FORGET TO CHANGE THE 'application.properties' FILE
        // SIMULATION : READ README
        // YOU NEED TO LAUNCH THREE SERVERS SEPARATELY

        // wait for server initialization
        Thread.sleep(2000);

        //create server (they are already launched we just want to simulate them, especially to send request to them)
        ArrayList<ServerService> servers = createServer();

        Thread.sleep(2000);

        // notifier les noeuds de la matrice des poids
        notifyServerOfWeightsUpdate(servers);

        Thread.sleep(2000);
        //create links between servers
        makeLinks(servers);


        Thread.sleep(2000);
        // A become source
        becomeSource(servers.get(0));
        // D become source
        becomeSource(servers.get(3));

        Thread.sleep(2000);
        removeSource(servers.get(0));

    }

    public static ArrayList<ServerService> createServer() {
        ArrayList<ServerService> res = new ArrayList<>();
        // serverA
        ServerService serverA = new ServerService(1);
        serverA.setServiceURL("http://localhost:8080");
        // serverB
        ServerService serverB = new ServerService(2);
        serverB.setServiceURL("http://localhost:8081");
        // serverA
        ServerService serverC = new ServerService(3);
        serverC.setServiceURL("http://localhost:8082");
        // serverB
        ServerService serverD = new ServerService(4);
        serverD.setServiceURL("http://localhost:8083");

        res.add(serverA);
        res.add(serverB);
        res.add(serverC);
        res.add(serverD);

        return res;
    }

    public static void notifyServerOfWeightsUpdate(ArrayList<ServerService> servers) {
        Weights weights = new Weights(6);
        weights.generateRandomWeights(1, 5);
        servers.forEach(serverService -> {
            try {
                sendWeight(weights, serverService);
            } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    public static void makeLinks(ArrayList<ServerService> servers) throws ExecutionException, InterruptedException, JsonProcessingException {
        // A-B
        addNeighbor(servers.get(0), servers.get(1));
        Thread.sleep(1000);
        // B-A
        addNeighbor(servers.get(1), servers.get(0));
        Thread.sleep(1000);

        //onEdgeUp
        sendOnEdgeUp(servers.get(0), servers.get(1));
        sendOnEdgeUp(servers.get(1), servers.get(0));


        // B-D
        addNeighbor(servers.get(1), servers.get(3));
        Thread.sleep(1000);
        // D-B
        addNeighbor(servers.get(3), servers.get(1));
        Thread.sleep(1000);

        //onEdgeUp
        sendOnEdgeUp(servers.get(1), servers.get(3));
        sendOnEdgeUp(servers.get(3), servers.get(1));

        // B-C
        addNeighbor(servers.get(1), servers.get(2));
        Thread.sleep(1000);
        // C-B
        addNeighbor(servers.get(2), servers.get(1));
        Thread.sleep(1000);

        //onEdgeUp
        sendOnEdgeUp(servers.get(1), servers.get(2));
        sendOnEdgeUp(servers.get(2), servers.get(1));

        // D-C
        addNeighbor(servers.get(3), servers.get(2));
        Thread.sleep(1000);
        // C-D
        addNeighbor(servers.get(2), servers.get(3));
        Thread.sleep(1000);

        //onEdgeUp
        sendOnEdgeUp(servers.get(3), servers.get(2));
        sendOnEdgeUp(servers.get(2), servers.get(3));
    }

    public static void removeNeighbor(ServerService from, ServerService ripNeighbor) throws InterruptedException, ExecutionException, JsonProcessingException {
        // simulation d'un envoi d'une requete http qui fait appel au controlleur du serveur
        // conversion en JSON
        String inputJson = JSONUtils.covertFromObjectToJson(ripNeighbor.getServerID());
        //System.out.println(inputJson);
        // create an http service to send request
        HttpService httpService = new HttpService(from.getServiceURL());
        // preparation de la requete
        HttpRequest request = httpService.preparePostRequest("/del_node", inputJson);
        // envoie de la requete
        CompletableFuture<HttpResponse<String>> response = httpService.sendPostRequest(request);
        // affichage du resultat
        System.out.println(response.get().body());
    }

    public static void addNeighbor(ServerService from, ServerService to) throws InterruptedException, ExecutionException, JsonProcessingException
    {
        // simulation d'un envoi d'une requete http qui fait appel au controlleur du serveur
        // conversion en JSON
        String inputJson = JSONUtils.covertFromObjectToJson(to);
        //System.out.println(inputJson);
        // create an http service to send request
        HttpService httpService = new HttpService(from.getServiceURL());
        // preparation de la requete
        HttpRequest request = httpService.preparePostRequest("/add_node", inputJson);
        // envoie de la requete
        CompletableFuture<HttpResponse<String>> response = httpService.sendPostRequest(request);
        // affichage du resultat
        System.out.println(response.get().body());
    }

    public static void sendOnEdgeUp(ServerService from, ServerService newNeighbor) throws JsonProcessingException, ExecutionException, InterruptedException {
        // simulation d'un envoi d'une requete http qui fait appel au controlleur du serveur
        // conversion en JSON
        String inputJson = JSONUtils.covertFromObjectToJson(newNeighbor);
        //System.out.println(inputJson);
        // create an http service to send request
        HttpService httpService = new HttpService(from.getServiceURL());
        // preparation de la requete
        HttpRequest request = httpService.preparePostRequest("/on_edge_up", inputJson);
        // envoie de la requete
        CompletableFuture<HttpResponse<String>> response = httpService.sendPostRequest(request);
        // affichage du resultat
        System.out.println(response.get().body());
    }

    public static void sendOnEdgeDown(ServerService from, ServerService ripNeighbor) throws JsonProcessingException, ExecutionException, InterruptedException {
        // simulation d'un envoi d'une requete http qui fait appel au controlleur du serveur
        // conversion en JSON
        String inputJson = JSONUtils.covertFromObjectToJson(ripNeighbor);
        //System.out.println(inputJson);
        // create an http service to send request
        HttpService httpService = new HttpService(from.getServiceURL());
        // preparation de la requete
        HttpRequest request = httpService.preparePostRequest("/on_edge_down", inputJson);
        // envoie de la requete
        CompletableFuture<HttpResponse<String>> response = httpService.sendPostRequest(request);
        // affichage du resultat
        System.out.println(response.get().body());
    }



    public static void becomeSource(ServerService from) throws InterruptedException, ExecutionException, JsonProcessingException {
        // create an http service to send request
        HttpService httpService = new HttpService(from.getServiceURL());
        // preparation de la requete
        HttpRequest request = httpService.preparePostRequestWithouBody("/add");
        // envoie de la requete
        CompletableFuture<HttpResponse<String>> response = httpService.sendPostRequest(request);
        // affichage du resultat
        System.out.println(response.get().body());
    }

    public static void removeSource(ServerService from) throws InterruptedException, ExecutionException, JsonProcessingException {
        // create an http service to send request
        HttpService httpService = new HttpService(from.getServiceURL());
        // preparation de la requete
        HttpRequest request = httpService.preparePostRequestWithouBody("/del");
        // envoie de la requete
        CompletableFuture<HttpResponse<String>> response = httpService.sendPostRequest(request);
        // affichage du resultat
        System.out.println(response.get().body());
    }

    public static void sendWeight(Weights weights, ServerService serverService) throws InterruptedException, ExecutionException, JsonProcessingException {
        // conversion en JSON
        String inputJson = JSONUtils.covertFromObjectToJson(weights);
        //System.out.println(inputJson);
        // create an http service to send request
        HttpService httpService = new HttpService(serverService.getServiceURL());
        // preparation de la requete
        HttpRequest request = httpService.preparePostRequest("/update_weights", inputJson);
        // envoie de la requete
        CompletableFuture<HttpResponse<String>> response = httpService.sendPostRequest(request);
        // affichage du resultat
        System.out.println(response.get().body());
    }

    public static void testDeserializationServer() throws JsonProcessingException {
        ServerService serverA = new ServerService(1);
        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(serverA);
        //System.out.println(json);
        final ServerService serverACopy = objectMapper.readValue(json, ServerService.class);
        System.out.println("COPIE " + serverACopy.toString());
    }

    public static void testDeserializationMAdd() throws JsonProcessingException {
        MAdd MAdd = new MAdd();
        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(MAdd);
        //System.out.println(json);
        final MAdd mAddCopy = objectMapper.readValue(json, MAdd.class);
        System.out.println("COPIE " + mAddCopy.toString());
    }

    public static void testDeserializationMDel() throws JsonProcessingException {
        MDel MDel = new MDel();
        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(MDel);
        //System.out.println(json);
        final MDel mDelCopy = objectMapper.readValue(json, MDel.class);
        System.out.println("COPIE " + mDelCopy.toString());
    }

    public static void testDeserializationPayloadMAdd() throws JsonProcessingException {
        ServerService server = new ServerService();
        MAdd MAdd = new MAdd();
        PayloadMAdd payload = new PayloadMAdd(server, MAdd);
        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(payload);
        //System.out.println(json);
        final PayloadMAdd payloadCopy = objectMapper.readValue(json, PayloadMAdd.class);
        System.out.println("COPIE " + payloadCopy.toString());
    }

    public static void testDeserializationPayloadMDel() throws JsonProcessingException {
        ServerService server = new ServerService();
        MDel MDel = new MDel();
        PayloadMDel payload = new PayloadMDel(server, MDel);
        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(payload);
        //System.out.println(json);
        final PayloadMDel payloadCopy = objectMapper.readValue(json, PayloadMDel.class);
        System.out.println("COPIE " + payloadCopy.toString());
    }

    public static void testDeserializationMap() throws JsonProcessingException {
        ServerService server = new ServerService(1);
        ServerService neighbor = new ServerService(2);
        server.getNeighbors().put(neighbor.getServerID(), neighbor);
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(server);
        //System.out.println(jsonResult);
        final ServerService serverACopy = mapper.readValue(jsonResult, ServerService.class);
        System.out.println("COPIE " + serverACopy.toString());
        System.out.println("VOISIN : " + serverACopy.getNeighbors());
    }

    public static void testAjoutPuisSuppressionDeVoisin() throws InterruptedException, ExecutionException, JsonProcessingException {
        Thread.sleep(2000);
        // serverA
        ServerService serverA = new ServerService(1);
        serverA.setServiceURL("http://localhost:8080");
        // serverB
        ServerService serverB = new ServerService(2);
        serverB.setServiceURL("http://localhost:8081");
        Thread.sleep(1000);
        // A-B
        addNeighbor(serverA,serverB);
        Thread.sleep(1000);
        // B-A
        addNeighbor(serverB, serverA);
        Thread.sleep(1000);

        removeNeighbor(serverA, serverB);
    }
}
